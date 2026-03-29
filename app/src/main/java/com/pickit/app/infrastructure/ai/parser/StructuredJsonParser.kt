package com.pickit.app.infrastructure.ai.parser

import com.pickit.app.infrastructure.ai.error.ModelJsonParseException
import com.pickit.app.infrastructure.ai.error.ModelNonJsonResponseException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Singleton
class StructuredJsonParser @Inject constructor(
    private val json: Json,
) {
    fun parseProductPayload(raw: String): ProductParsePayload {
        val direct = runCatching { json.decodeFromString(ProductParsePayload.serializer(), raw.trim()) }
        if (direct.isSuccess) {
            return direct.getOrThrow()
        }

        val cleaned = extractJsonObject(raw)
        return runCatching {
            json.decodeFromString(ProductParsePayload.serializer(), cleaned)
        }.getOrElse { error ->
            throw ModelJsonParseException("模型返回 JSON 解析失败", error)
        }
    }

    fun extractJsonObject(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) {
            throw ModelNonJsonResponseException("模型返回了空文本")
        }

        val fenceRemoved = trimmed
            .removePrefix("```json")
            .removePrefix("```JSON")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        val firstBrace = fenceRemoved.indexOf('{')
        val lastBrace = fenceRemoved.lastIndexOf('}')
        if (firstBrace == -1 || lastBrace == -1 || lastBrace <= firstBrace) {
            throw ModelNonJsonResponseException("模型未返回可解析的 JSON 对象")
        }

        return fenceRemoved.substring(firstBrace, lastBrace + 1)
    }
}

@Serializable
data class ProductParsePayload(
    val title: String? = null,
    val brand: String? = null,
    val category: String? = null,
    val subCategory: String? = null,
    val platform: String? = null,
    val shopName: String? = null,
    val priceText: String? = null,
    val priceValue: Double? = null,
    val currency: String? = "CNY",
    val spec: String? = null,
    val summary: String? = null,
    val recommendationReason: String? = null,
    val tags: List<String> = emptyList(),
    val confidence: Float? = null,
    val rawText: String? = null,
)
