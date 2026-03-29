package com.pickit.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pickit.app.data.local.entity.PriceHistoryEntity

@Dao
interface PriceHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: PriceHistoryEntity)

    @Query("SELECT * FROM price_history WHERE product_id = :productId ORDER BY recorded_at DESC")
    suspend fun getByProductId(productId: String): List<PriceHistoryEntity>

    @Query("SELECT * FROM price_history WHERE product_id = :productId ORDER BY recorded_at DESC LIMIT 1")
    suspend fun getLatestByProductId(productId: String): PriceHistoryEntity?
}
