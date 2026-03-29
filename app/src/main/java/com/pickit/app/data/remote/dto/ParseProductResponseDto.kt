package com.pickit.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ParseProductResponseDto(
    val success: Boolean,
    val data: ParsedProductDataDto? = null,
)

@Serializable
data class ParsedProductDataDto(
    val title: String? = null,
    val brand: String? = null,
    val category: String? = null,
    @SerialName("sub_category") val subCategory: String? = null,
    val platform: String? = null,
    @SerialName("shop_name") val shopName: String? = null,
    @SerialName("price_text") val priceText: String? = null,
    @SerialName("price_value") val priceValue: Double? = null,
    val currency: String? = "CNY",
    val spec: String? = null,
    val summary: String? = null,
    @SerialName("recommendation_reason") val recommendationReason: String? = null,
    val tags: List<String> = emptyList(),
    val confidence: Float? = null,
    @SerialName("raw_text") val rawText: String? = null,
)
