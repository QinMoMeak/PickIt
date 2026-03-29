package com.pickit.app.infrastructure.ai.provider.zhipu

import android.os.SystemClock
import android.util.Log
import com.pickit.app.domain.model.ModelProvider
import com.pickit.app.domain.model.ModelProviderConfig
import com.pickit.app.domain.model.ParsedProductResult
import com.pickit.app.domain.model.VisionParseRequest
import com.pickit.app.infrastructure.ai.error.AuthenticationFailureException
import com.pickit.app.infrastructure.ai.error.EmptyModelResponseException
import com.pickit.app.infrastructure.ai.error.ModelJsonParseException
import com.pickit.app.infrastructure.ai.error.ModelNonJsonResponseException
import com.pickit.app.infrastructure.ai.error.NetworkRequestException
import com.pickit.app.infrastructure.ai.error.ProviderConfigurationException
import com.pickit.app.infrastructure.ai.error.ProviderHttpException
import com.pickit.app.infrastructure.ai.error.RequestFormatException
import com.pickit.app.infrastructure.ai.prompt.ProductParsePromptBuilder
import com.pickit.app.infrastructure.ai.provider.VisionProviderClient
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

@Singleton
class ZhipuVisionClient @Inject constructor(
    private val baseClient: OkHttpClient,
    private val json: Json,
    private val promptBuilder: ProductParsePromptBuilder,
    private val responseParser: ZhipuResponseParser,
) : VisionProviderClient {
    override val provider: ModelProvider = ModelProvider.ZHIPU

    override suspend fun parse(
        config: ModelProviderConfig,
        request: VisionParseRequest,
    ): Result<ParsedProductResult> = runCatching {
        require(config.baseUrl.isNotBlank()) { "请先配置 AI_BASE_URL" }
        require(config.model.isNotBlank()) { "请先配置 AI_MODEL" }
        require(request.imageUrl != null || request.imageBase64 != null) { "缺少图片输入" }

        val client = baseClient.newBuilder()
            .callTimeout(config.timeoutSeconds, TimeUnit.SECONDS)
            .connectTimeout(config.timeoutSeconds, TimeUnit.SECONDS)
            .readTimeout(config.timeoutSeconds, TimeUnit.SECONDS)
            .writeTimeout(config.timeoutSeconds, TimeUnit.SECONDS)
            .build()

        val payload = ZhipuChatCompletionRequest(
            model = config.model,
            messages = listOf(
                ZhipuMessage(
                    role = "system",
                    content = buildJsonArray {
                        add(
                            buildJsonObject {
                                put("type", "text")
                                put("text", promptBuilder.buildSystemPrompt())
                            },
                        )
                    },
                ),
                ZhipuMessage(
                    role = "user",
                    content = buildJsonArray {
                        add(
                            buildJsonObject {
                                put("type", "image_url")
                                putJsonObject("image_url") {
                                    put("url", request.imageUrl ?: request.imageBase64!!)
                                }
                            },
                        )
                        add(
                            buildJsonObject {
                                put("type", "text")
                                put("text", promptBuilder.buildUserPrompt(request))
                            },
                        )
                    },
                ),
            ),
            temperature = config.temperature,
            maxTokens = config.maxTokens,
            thinking = ZhipuThinking(if (config.enabledThinking) "enabled" else "disabled"),
            stream = false,
        )

        val endpoint = buildEndpoint(config.baseUrl)
        val requestBody = json.encodeToString(ZhipuChatCompletionRequest.serializer(), payload)
        Log.i(
            "ZhipuVisionClient",
            "request_start provider=zhipu model=${config.model} endpoint=$endpoint apiKey=${maskApiKey(config.apiKey)}",
        )

        val startedAt = SystemClock.elapsedRealtime()
        val rawResponse = executeWithRetry(
            client = client,
            endpoint = endpoint,
            apiKey = config.apiKey,
            body = requestBody,
            model = config.model,
        )
        val response = json.decodeFromString(ZhipuChatCompletionResponse.serializer(), rawResponse)
        val parsed = responseParser.parse(config, rawResponse, response)
        val duration = SystemClock.elapsedRealtime() - startedAt

        Log.i(
            "ZhipuVisionClient",
            "request_success provider=zhipu model=${config.model} durationMs=$duration jsonParsed=true rawPreview=${rawResponse.take(500)}",
        )
        parsed
    }.onFailure { error ->
        Log.e(
            "ZhipuVisionClient",
            "request_failure provider=zhipu reason=${error::class.simpleName} message=${error.message}",
        )
    }

    private fun executeWithRetry(
        client: OkHttpClient,
        endpoint: String,
        apiKey: String,
        body: String,
        model: String,
    ): String {
        var lastError: Throwable? = null
        repeat(2) { attempt ->
            val request = Request.Builder()
                .url(endpoint)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(body.toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()

            val startedAt = SystemClock.elapsedRealtime()
            try {
                client.newCall(request).execute().use { response ->
                    val rawText = response.body?.string().orEmpty()
                    val duration = SystemClock.elapsedRealtime() - startedAt
                    Log.i(
                        "ZhipuVisionClient",
                        "http_result provider=zhipu model=$model status=${response.code} durationMs=$duration rawPreview=${rawText.take(500)}",
                    )

                    if (!response.isSuccessful) {
                        throw when (response.code) {
                            400 -> RequestFormatException("智谱请求格式错误")
                            401, 403 -> AuthenticationFailureException("智谱鉴权失败")
                            else -> ProviderHttpException(
                                statusCode = response.code,
                                message = "智谱接口请求失败，HTTP ${response.code}",
                            )
                        }
                    }
                    if (rawText.isBlank()) {
                        throw EmptyModelResponseException("智谱接口返回为空")
                    }
                    return rawText
                }
            } catch (error: AuthenticationFailureException) {
                throw error
            } catch (error: RequestFormatException) {
                throw error
            } catch (error: ProviderHttpException) {
                throw error
            } catch (error: EmptyModelResponseException) {
                throw error
            } catch (error: ModelNonJsonResponseException) {
                throw error
            } catch (error: ModelJsonParseException) {
                throw error
            } catch (error: IOException) {
                lastError = error
                if (attempt == 1) {
                    throw NetworkRequestException("智谱接口网络请求失败", error)
                }
            }
        }

        throw NetworkRequestException("智谱接口网络请求失败", lastError)
    }

    private fun buildEndpoint(baseUrl: String): String {
        val normalized = baseUrl.trim().trimEnd('/')
        if (normalized.isBlank()) {
            throw ProviderConfigurationException("请先配置 AI_BASE_URL")
        }
        return if (normalized.endsWith("/chat/completions")) {
            normalized
        } else {
            "$normalized/chat/completions"
        }
    }

    private fun maskApiKey(apiKey: String): String = when {
        apiKey.length <= 8 -> "****"
        else -> apiKey.take(4) + "****" + apiKey.takeLast(4)
    }
}
