package com.pickit.app.data.repository

import androidx.room.withTransaction
import com.pickit.app.data.local.dao.PriceHistoryDao
import com.pickit.app.data.local.dao.ProductDao
import com.pickit.app.data.local.dao.TagDao
import com.pickit.app.data.local.db.PickItDatabase
import com.pickit.app.data.mapper.ProductMapper
import com.pickit.app.domain.model.ProductFilter
import com.pickit.app.domain.model.ProductItem
import com.pickit.app.domain.repository.ProductRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Singleton
class RoomProductRepository @Inject constructor(
    private val database: PickItDatabase,
    private val productDao: ProductDao,
    private val tagDao: TagDao,
    private val priceHistoryDao: PriceHistoryDao,
    private val mapper: ProductMapper,
) : ProductRepository {
    override fun observeProducts(): Flow<List<ProductItem>> =
        productDao.observeProducts().map { products -> products.map(mapper::toDomain) }

    override suspend fun getProduct(id: String): ProductItem? =
        productDao.getProduct(id)?.let(mapper::toDomain)

    override suspend fun saveProduct(product: ProductItem) {
        upsertProductGraph(product)
    }

    override suspend fun updateProduct(product: ProductItem) {
        upsertProductGraph(product)
    }

    override suspend fun deleteProduct(id: String) {
        productDao.deleteById(id)
    }

    override suspend fun search(query: String, filter: ProductFilter): List<ProductItem> {
        val normalized = query.trim()
        return productDao.observeProducts().first()
            .map(mapper::toDomain)
            .filter { product ->
                val matchesQuery = normalized.isBlank() || listOfNotNull(
                    product.title,
                    product.brand,
                    product.shopName,
                    product.summary,
                    product.recommendationReason,
                    product.sourceNote,
                ).any { it.contains(normalized, ignoreCase = true) } ||
                    product.tags.any { it.name.contains(normalized, ignoreCase = true) }
                val matchesPlatform = filter.platform == null || product.platform == filter.platform
                val matchesStatus = filter.status == null || product.status == filter.status
                val matchesTag = filter.tag == null || product.tags.any { it.name.equals(filter.tag, ignoreCase = true) }
                val matchesPrice = filter.priceRange == null ||
                    product.currentPriceValue?.let { value -> value in filter.priceRange } == true

                matchesQuery && matchesPlatform && matchesStatus && matchesTag && matchesPrice
            }
    }

    private suspend fun upsertProductGraph(product: ProductItem) {
        database.withTransaction {
            productDao.insert(mapper.toEntity(product))

            val tagEntities = mapper.toTagEntities(product)
            tagDao.deleteRefsForProduct(product.id)
            if (tagEntities.isNotEmpty()) {
                tagDao.upsertTags(tagEntities)
                tagDao.upsertRefs(mapper.toTagRefs(product.id, tagEntities))
            }

            val latest = priceHistoryDao.getLatestByProductId(product.id)
            val hasPrice = product.currentPriceText != null || product.currentPriceValue != null
            val sameAsLatest = latest?.priceText == product.currentPriceText &&
                latest?.priceValue == product.currentPriceValue
            if (hasPrice && !sameAsLatest) {
                priceHistoryDao.insert(mapper.toPriceHistoryEntity(product, recordedAt = product.updatedAt))
            }
        }
    }
}
