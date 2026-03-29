package com.pickit.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "product_tag_ref",
    primaryKeys = ["product_id", "tag_id"],
    indices = [
        Index(value = ["product_id"]),
        Index(value = ["tag_id"]),
    ],
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["product_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tag_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class ProductTagCrossRef(
    val product_id: String,
    val tag_id: String,
)
