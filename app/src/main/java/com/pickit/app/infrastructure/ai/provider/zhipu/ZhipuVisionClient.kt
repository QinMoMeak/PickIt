package com.pickit.app.infrastructure.ai.provider.zhipu

import android.util.Log
import com.pickit.app.domain.model.ModelProvider
import com.pickit.app.domain.model.ModelProviderConfig
import com.pickit.app.domain.model.ParsedProductResult
import com.pickit.app.domain.model.VisionParseRequest
import com.pickit.app.infrastructure.ai.error.NetworkRequestException
import com.pickit.app.infrastructure.ai.error.ProviderConfigurationException
import com.pickit.app.infrastructure.ai.error.ProviderHttpException
import com.pickit.app.infrastructure.ai.prompt.ProductParsePromptBuilder
import com.pickit.app.infrastructure.ai.provider.VisionProviderClient
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
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
                    content = JsonPrimitive(promptBuilder.buildSystemPrompt()),
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
        val body = json.encodeToString(ZhipuChatCompletionRequest.serializer(), payload)
        Log.i("ZhipuVisionClient", "POST $endpoint model=${config.model} provider=zhipu apiKey=${maskApiKey(config.apiKey)}")

        val rawResponse = executeWithRetry(
            client = client,
            endpoint = endpoint,
            apiKey = config.apiKey,
            body = body,
        )
        val response = json.decodeFromString(ZhipuChatCompletionResponse.serializer(), rawResponse)
        responseParser.parse(config, rawResponse, response)
    }

    private fun executeWithRetry(
        client: OkHttpClient,
        endpoint: String,
        apiKey: String,
        body: String,
    ): String {
        var lastError: Throwable? = null
        repeat(2) { attempt ->
            val request = Request.Builder()
                .url(endpoint)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(body.toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    val rawText = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        throw ProviderHttpException(
                            statusCode = response.code,
                            message = "智谱接口请求失败，HTTP ${response.code}",
                        )
                    }
                    return rawText
                }
            } catch (error: ProviderHttpException) {
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
        require(normalized.isNotBlank()) { throw ProviderConfigurationException("请先配置 AI_BASE_URL") }
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
