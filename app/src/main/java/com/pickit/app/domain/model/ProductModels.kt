package com.pickit.app.domain.model

data class ProductItem(
    val id: String,
    val title: String,
    val brand: String? = null,
    val category: String? = null,
    val subCategory: String? = null,
    val platform: Platform = Platform.OTHER,
    val shopName: String? = null,
    val currentPriceText: String? = null,
    val currentPriceValue: Double? = null,
    val currency: String = "CNY",
    val spec: String? = null,
    val summary: String? = null,
    val recommendationReason: String? = null,
    val tags: List<Tag> = emptyList(),
    val status: ProductStatus = ProductStatus.NOT_PURCHASED,
    val sourceType: SourceType = SourceType.IMAGE,
    val sourceNote: String? = null,
    val confidence: Float? = null,
    val aiRawJson: String? = null,
    val priceHistory: List<PriceHistory> = emptyList(),
    val createdAt: String,
    val updatedAt: String,
)

data class Tag(
    val id: String,
    val name: String,
)

data class PriceHistory(
    val id: String,
    val productId: String,
    val priceText: String? = null,
    val priceValue: Double? = null,
    val currency: String = "CNY",
    val platform: Platform = Platform.OTHER,
    val shopName: String? = null,
    val sourceNote: String? = null,
    val recordedAt: String,
)

data class ParsedProductDraft(
    val title: String = "",
    val brand: String? = null,
    val category: String? = null,
    val subCategory: String? = null,
    val platform: Platform = Platform.OTHER,
    val shopName: String? = null,
    val priceText: String? = null,
    val priceValue: Double? = null,
    val currency: String = "CNY",
    val spec: String? = null,
    val summary: String? = null,
    val recommendationReason: String? = null,
    val tags: List<String> = emptyList(),
    val confidence: Float? = null,
    val rawText: String? = null,
    val sourceNote: String? = null,
)

data class ProductFilter(
    val platform: Platform? = null,
    val status: ProductStatus? = null,
    val tag: String? = null,
    val priceRange: ClosedFloatingPointRange<Double>? = null,
)
