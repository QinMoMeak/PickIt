package com.pickit.app.domain.repository

import com.pickit.app.domain.model.ProductFilter
import com.pickit.app.domain.model.ProductItem
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun observeProducts(): Flow<List<ProductItem>>
    suspend fun getProduct(id: String): ProductItem?
    suspend fun saveProduct(product: ProductItem)
    suspend fun updateProduct(product: ProductItem)
    suspend fun deleteProduct(id: String)
    suspend fun search(query: String, filter: ProductFilter): List<ProductItem>
}
