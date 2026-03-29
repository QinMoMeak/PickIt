package com.pickit.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.pickit.app.data.local.entity.ProductEntity
import com.pickit.app.data.local.relation.ProductWithTags
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Transaction
    @Query("SELECT * FROM product_items ORDER BY updated_at DESC")
    fun observeProducts(): Flow<List<ProductWithTags>>

    @Transaction
    @Query("SELECT * FROM product_items WHERE id = :id")
    suspend fun getProduct(id: String): ProductWithTags?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: ProductEntity)

    @Update
    suspend fun update(product: ProductEntity)

    @Query("DELETE FROM product_items WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query(
        """
        SELECT * FROM product_items
        WHERE title LIKE '%' || :query || '%'
           OR brand LIKE '%' || :query || '%'
           OR shop_name LIKE '%' || :query || '%'
           OR summary LIKE '%' || :query || '%'
           OR recommendation_reason LIKE '%' || :query || '%'
           OR source_note LIKE '%' || :query || '%'
        ORDER BY updated_at DESC
        """,
    )
    suspend fun search(query: String): List<ProductEntity>
}
