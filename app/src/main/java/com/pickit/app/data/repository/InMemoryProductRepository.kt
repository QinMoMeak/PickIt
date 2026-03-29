package com.pickit.app.data.repository

import com.pickit.app.core.util.SampleData
import com.pickit.app.domain.model.ProductFilter
import com.pickit.app.domain.model.ProductItem
import com.pickit.app.domain.repository.ProductRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Singleton
class InMemoryProductRepository @Inject constructor() : ProductRepository {
    private val products = MutableStateFlow(SampleData.products)

    override fun observeProducts(): Flow<List<ProductItem>> = products.asStateFlow()

    override suspend fun getProduct(id: String): ProductItem? = products.value.firstOrNull { it.id == id }

    override suspend fun saveProduct(product: ProductItem) {
        products.update { current -> listOf(product) + current }
    }

    override suspend fun updateProduct(product: ProductItem) {
        products.update { current -> current.map { if (it.id == product.id) product else it } }
    }

    override suspend fun deleteProduct(id: String) {
        products.update { current -> current.filterNot { it.id == id } }
    }

    override suspend fun search(query: String, filter: ProductFilter): List<ProductItem> {
        val normalized = query.trim()
        return products.value.filter { product ->
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
            val matchesTag = filter.tag == null || product.tags.any { it.name == filter.tag }
            val matchesPrice = filter.priceRange == null ||
                product.currentPriceValue?.let { it in filter.priceRange } == true

            matchesQuery && matchesPlatform && matchesStatus && matchesTag && matchesPrice
        }
    }
}
