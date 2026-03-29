package com.pickit.app.infrastructure.ai.provider.zhipu

import com.pickit.app.domain.model.ModelProvider
import com.pickit.app.domain.model.ModelProviderConfig
import com.pickit.app.domain.model.ParsedProductResult
import com.pickit.app.domain.model.Platform
import com.pickit.app.infrastructure.ai.error.ModelNonJsonResponseException
import com.pickit.app.infrastructure.ai.parser.StructuredJsonParser
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

@Singleton
class ZhipuResponseParser @Inject constructor(
    private val structuredJsonParser: StructuredJsonParser,
) {
    fun parse(
        config: ModelProviderConfig,
        rawResponse: String,
        response: ZhipuChatCompletionResponse,
    ): ParsedProductResult {
        val message = response.choices.firstOrNull()?.message
            ?: throw ModelNonJsonResponseException("智谱返回中没有 choices.message")
        val modelText = extractAssistantText(message.content)
        val payload = structuredJsonParser.parseProductPayload(modelText)

        return ParsedProductResult(
            title = payload.title,
            brand = payload.brand,
            category = payload.category,
            subCategory = payload.subCategory,
            platform = payload.platform.toPlatform(),
            shopName = payload.shopName,
            priceText = payload.priceText,
            priceValue = payload.priceValue,
            currency = payload.currency ?: "CNY",
            spec = payload.spec,
            summary = payload.summary,
            recommendationReason = payload.recommendationReason,
            tags = payload.tags.filter { it.isNotBlank() },
            confidence = payload.confidence?.coerceIn(0f, 1f),
            rawText = payload.rawText ?: modelText,
            rawModelResponse = rawResponse,
            provider = ModelProvider.ZHIPU,
            model = response.model ?: config.model,
        )
    }

    private fun extractAssistantText(content: JsonElement?): String {
        if (content == null) {
            throw ModelNonJsonResponseException("智谱返回内容为空")
        }

        return when (content) {
            is JsonPrimitive -> content.content
            is JsonArray -> content.joinToString("\n") { part ->
                when (part) {
                    is JsonPrimitive -> part.content
                    is JsonObject -> part["text"]?.jsonPrimitive?.content.orEmpty()
                    else -> part.toString()
                }
            }
            is JsonObject -> content["text"]?.jsonPrimitive?.content ?: content.toString()
            else -> content.toString()
        }
    }

    private fun String?.toPlatform(): Platform = this?.let { raw ->
        Platform.entries.firstOrNull { it.name.equals(raw.trim(), ignoreCase = true) }
    } ?: Platform.OTHER
}
