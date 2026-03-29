package com.pickit.app.domain.model

data class ModelProviderConfig(
    val provider: ModelProvider,
    val baseUrl: String,
    val apiKey: String,
    val model: String,
    val timeoutSeconds: Long = 60,
    val enabledThinking: Boolean = false,
    val maxTokens: Int? = null,
    val temperature: Double? = null,
)

enum class ParseScene {
    PRODUCT_RECOGNITION,
}

data class VisionParseRequest(
    val imageUrl: String? = null,
    val imageBase64: String? = null,
    val userNote: String? = null,
    val parseScene: ParseScene = ParseScene.PRODUCT_RECOGNITION,
    val preferredPlatformCandidates: List<Platform> = emptyList(),
    val structuredOutputSchemaVersion: Int = 1,
)

data class ParsedProductResult(
    val title: String? = null,
    val brand: String? = null,
    val category: String? = null,
    val subCategory: String? = null,
    val platform: Platform = Platform.OTHER,
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
    val rawModelResponse: String? = null,
    val provider: ModelProvider,
    val model: String,
)

fun ParsedProductResult.toDraft(sourceNote: String? = null): ParsedProductDraft = ParsedProductDraft(
    title = title.orEmpty(),
    brand = brand,
    category = category,
    subCategory = subCategory,
    platform = platform,
    shopName = shopName,
    priceText = priceText,
    priceValue = priceValue,
    currency = currency ?: "CNY",
    spec = spec,
    summary = summary,
    recommendationReason = recommendationReason,
    tags = tags,
    confidence = confidence,
    rawText = rawText ?: rawModelResponse,
    sourceNote = sourceNote,
)
