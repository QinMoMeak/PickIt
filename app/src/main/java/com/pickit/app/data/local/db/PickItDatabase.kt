package com.pickit.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pickit.app.data.local.dao.ParseLogDao
import com.pickit.app.data.local.dao.PriceHistoryDao
import com.pickit.app.data.local.dao.ProductDao
import com.pickit.app.data.local.dao.TagDao
import com.pickit.app.data.local.entity.ParseLogEntity
import com.pickit.app.data.local.entity.PriceHistoryEntity
import com.pickit.app.data.local.entity.ProductEntity
import com.pickit.app.data.local.entity.ProductTagCrossRef
import com.pickit.app.data.local.entity.TagEntity

@Database(
    entities = [
        ProductEntity::class,
        TagEntity::class,
        ProductTagCrossRef::class,
        PriceHistoryEntity::class,
        ParseLogEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class PickItDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun tagDao(): TagDao
    abstract fun priceHistoryDao(): PriceHistoryDao
    abstract fun parseLogDao(): ParseLogDao
}
