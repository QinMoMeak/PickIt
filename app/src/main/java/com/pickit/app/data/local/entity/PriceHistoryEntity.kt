package com.pickit.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "price_history",
    indices = [Index(value = ["product_id"])],
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["product_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class PriceHistoryEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "product_id") val productId: String,
    @ColumnInfo(name = "price_text") val priceText: String? = null,
    @ColumnInfo(name = "price_value") val priceValue: Double? = null,
    val currency: String = "CNY",
    val platform: String? = null,
    @ColumnInfo(name = "shop_name") val shopName: String? = null,
    @ColumnInfo(name = "source_note") val sourceNote: String? = null,
    @ColumnInfo(name = "recorded_at") val recordedAt: String,
)
