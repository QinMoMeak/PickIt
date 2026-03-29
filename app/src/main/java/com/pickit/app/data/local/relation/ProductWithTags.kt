package com.pickit.app.data.local.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.pickit.app.data.local.entity.PriceHistoryEntity
import com.pickit.app.data.local.entity.ProductEntity
import com.pickit.app.data.local.entity.ProductTagCrossRef
import com.pickit.app.data.local.entity.TagEntity

data class ProductWithTags(
    @Embedded val product: ProductEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ProductTagCrossRef::class,
            parentColumn = "product_id",
            entityColumn = "tag_id",
        ),
    )
    val tags: List<TagEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "product_id",
    )
    val priceHistory: List<PriceHistoryEntity>,
)
