package com.pickit.app.data.mapper

import com.pickit.app.data.local.entity.PriceHistoryEntity
import com.pickit.app.data.local.entity.ProductEntity
import com.pickit.app.data.local.entity.ProductTagCrossRef
import com.pickit.app.data.local.entity.TagEntity
import com.pickit.app.data.local.relation.ProductWithTags
import com.pickit.app.domain.model.Platform
import com.pickit.app.domain.model.PriceHistory
import com.pickit.app.domain.model.ProductItem
import com.pickit.app.domain.model.ProductStatus
import com.pickit.app.domain.model.SourceType
import com.pickit.app.domain.model.Tag
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class ProductMapper @Inject constructor() {
    fun toDomain(relation: ProductWithTags): ProductItem = ProductItem(
        id = relation.product.id,
        title = relation.product.title,
        brand = relation.product.brand,
        category = relation.product.category,
        subCategory = relation.product.subCategory,
        platform = relation.product.platform.toPlatform(),
        shopName = relation.product.shopName,
        currentPriceText = relation.product.currentPriceText,
        currentPriceValue = relation.product.currentPriceValue,
        currency = relation.product.currency,
        spec = relation.product.spec,
        summary = relation.product.summary,
        recommendationReason = relation.product.recommendationReason,
        tags = relation.tags.map { Tag(id = it.id, name = it.name) }.sortedBy { it.name },
        status = relation.product.status.toProductStatus(),
        sourceType = relation.product.sourceType.toSourceType(),
        sourceNote = relation.product.sourceNote,
        confidence = relation.product.confidence,
        aiRawJson = relation.product.aiRawJson,
        priceHistory = relation.priceHistory.map(::toDomainPriceHistory).sortedByDescending { it.recordedAt },
        createdAt = relation.product.createdAt,
        updatedAt = relation.product.updatedAt,
    )

    fun toEntity(product: ProductItem): ProductEntity = ProductEntity(
        id = product.id,
        title = product.title,
        brand = product.brand,
        category = product.category,
        subCategory = product.subCategory,
        platform = product.platform.name,
        shopName = product.shopName,
        currentPriceText = product.currentPriceText,
        currentPriceValue = product.currentPriceValue,
        currency = product.currency,
        spec = product.spec,
        summary = product.summary,
        recommendationReason = product.recommendationReason,
        status = product.status.name,
        sourceType = product.sourceType.name,
        sourceNote = product.sourceNote,
        confidence = product.confidence,
        aiRawJson = product.aiRawJson,
        createdAt = product.createdAt,
        updatedAt = product.updatedAt,
    )

    fun toTagEntities(product: ProductItem): List<TagEntity> = product.tags
        .map { tag ->
            val normalizedName = normalizeTagName(tag.name)
            TagEntity(
                id = if (tag.id.isBlank()) UUID.randomUUID().toString() else tag.id,
                name = tag.name.trim(),
                normalizedName = normalizedName,
                createdAt = Instant.now().toString(),
            )
        }
        .distinctBy { it.normalizedName }

    fun toTagRefs(productId: String, tags: List<TagEntity>): List<ProductTagCrossRef> =
        tags.map { tag -> ProductTagCrossRef(product_id = productId, tag_id = tag.id) }

    fun toPriceHistoryEntity(product: ProductItem, recordedAt: String = product.updatedAt): PriceHistoryEntity =
        PriceHistoryEntity(
            id = UUID.randomUUID().toString(),
            productId = product.id,
            priceText = product.currentPriceText,
            priceValue = product.currentPriceValue,
            currency = product.currency,
            platform = product.platform.name,
            shopName = product.shopName,
            sourceNote = product.sourceNote,
            recordedAt = recordedAt,
        )

    fun toDomainPriceHistory(entity: PriceHistoryEntity): PriceHistory = PriceHistory(
        id = entity.id,
        productId = entity.productId,
        priceText = entity.priceText,
        priceValue = entity.priceValue,
        currency = entity.currency,
        platform = entity.platform.toPlatform(),
        shopName = entity.shopName,
        sourceNote = entity.sourceNote,
        recordedAt = entity.recordedAt,
    )

    private fun String?.toPlatform(): Platform = this?.let { value ->
        Platform.entries.firstOrNull { it.name == value }
    } ?: Platform.OTHER

    private fun String?.toProductStatus(): ProductStatus = this?.let { value ->
        ProductStatus.entries.firstOrNull { it.name == value }
    } ?: ProductStatus.NOT_PURCHASED

    private fun String?.toSourceType(): SourceType = this?.let { value ->
        SourceType.entries.firstOrNull { it.name == value }
    } ?: SourceType.IMAGE

    private fun normalizeTagName(name: String): String = name.trim().lowercase()
}
