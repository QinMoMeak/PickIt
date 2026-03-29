package com.pickit.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pickit.app.data.local.entity.ProductTagCrossRef
import com.pickit.app.data.local.entity.TagEntity

@Dao
interface TagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTags(tags: List<TagEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRefs(refs: List<ProductTagCrossRef>)

    @Query("DELETE FROM product_tag_ref WHERE product_id = :productId")
    suspend fun deleteRefsForProduct(productId: String)

    @Query("SELECT * FROM tags ORDER BY name ASC")
    suspend fun getAllTags(): List<TagEntity>
}
