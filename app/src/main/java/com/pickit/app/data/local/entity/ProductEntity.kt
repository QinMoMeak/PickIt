package com.pickit.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "product_items")
data class ProductEntity(
    @PrimaryKey val id: String,
    val title: String,
    val brand: String? = null,
    val category: String? = null,
    @ColumnInfo(name = "sub_category") val subCategory: String? = null,
    val platform: String? = null,
    @ColumnInfo(name = "shop_name") val shopName: String? = null,
    @ColumnInfo(name = "current_price_text") val currentPriceText: String? = null,
    @ColumnInfo(name = "current_price_value") val currentPriceValue: Double? = null,
    val currency: String = "CNY",
    val spec: String? = null,
    val summary: String? = null,
    @ColumnInfo(name = "recommendation_reason") val recommendationReason: String? = null,
    val status: String = "NOT_PURCHASED",
    @ColumnInfo(name = "source_type") val sourceType: String = "IMAGE",
    @ColumnInfo(name = "source_note") val sourceNote: String? = null,
    val confidence: Float? = null,
    @ColumnInfo(name = "ai_raw_json") val aiRawJson: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "updated_at") val updatedAt: String,
)
