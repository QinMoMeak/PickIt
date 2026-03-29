package com.pickit.app.infrastructure.ai.provider.openai_compatible

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
class OpenAiCompatibleVisionClient @Inject constructor(
    private val baseClient: OkHttpClient,
    private val json: Json,
    private val promptBuilder: ProductParsePromptBuilder,
    private val responseParser: OpenAiCompatibleResponseParser,
) : VisionProviderClient {
    override val provider: ModelProvider = ModelProvider.OPENAI_COMPATIBLE

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

        val payload = OpenAiCompatibleChatCompletionRequest(
            model = config.model,
            messages = listOf(
                OpenAiCompatibleMessage(
                    role = "system",
                    content = kotlinx.serialization.json.JsonPrimitive(promptBuilder.buildSystemPrompt()),
                ),
                OpenAiCompatibleMessage(
                    role = "user",
                    content = buildJsonArray {
                        add(
                            buildJsonObject {
                                put("type", "image_url")
                                putJsonObject("image_url") {
                                    put("url", request.imageUrl ?: request.imageBase64.toDataUrl())
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
            stream = false,
        )

        val endpoint = buildEndpoint(config.baseUrl)
        val requestBody = json.encodeToString(OpenAiCompatibleChatCompletionRequest.serializer(), payload)
        Log.i(
            "OpenAiCompatibleVisionClient",
            "request_start provider=openai_compatible model=${config.model} endpoint=$endpoint apiKey=${maskApiKey(config.apiKey)}",
        )

        val startedAt = SystemClock.elapsedRealtime()
        val rawResponse = executeWithRetry(
            client = client,
            endpoint = endpoint,
            apiKey = config.apiKey,
            body = requestBody,
            model = config.model,
        )
        val response = json.decodeFromString(OpenAiCompatibleChatCompletionResponse.serializer(), rawResponse)
        val parsed = responseParser.parse(config, rawResponse, response)
        val duration = SystemClock.elapsedRealtime() - startedAt

        Log.i(
            "OpenAiCompatibleVisionClient",
            "request_success provider=openai_compatible model=${config.model} durationMs=$duration jsonParsed=true rawPreview=${rawResponse.take(500)}",
        )
        parsed
    }.onFailure { error ->
        Log.e(
            "OpenAiCompatibleVisionClient",
            "request_failure provider=openai_compatible reason=${error::class.simpleName} message=${error.message}",
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
                        "OpenAiCompatibleVisionClient",
                        "http_result provider=openai_compatible model=$model status=${response.code} durationMs=$duration rawPreview=${rawText.take(500)}",
                    )

                    if (!response.isSuccessful) {
                        throw when (response.code) {
                            400 -> RequestFormatException("OpenAI-compatible 请求格式错误")
                            401, 403 -> AuthenticationFailureException("OpenAI-compatible 鉴权失败")
                            else -> ProviderHttpException(
                                statusCode = response.code,
                                message = "OpenAI-compatible 请求失败，HTTP ${response.code}",
                            )
                        }
                    }
                    if (rawText.isBlank()) {
                        throw EmptyModelResponseException("模型接口返回为空")
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
                    throw NetworkRequestException("OpenAI-compatible 网络请求失败", error)
                }
            }
        }

        throw NetworkRequestException("OpenAI-compatible 网络请求失败", lastError)
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

    private fun String?.toDataUrl(): String {
        val value = this?.trim().orEmpty()
        return if (value.startsWith("data:")) value else "data:image/jpeg;base64,$value"
    }

    private fun maskApiKey(apiKey: String): String = when {
        apiKey.length <= 8 -> "****"
        else -> apiKey.take(4) + "****" + apiKey.takeLast(4)
    }
}
