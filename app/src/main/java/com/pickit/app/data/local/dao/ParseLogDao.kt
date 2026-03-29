package com.pickit.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.pickit.app.data.local.entity.ParseLogEntity

@Dao
interface ParseLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: ParseLogEntity)
}
