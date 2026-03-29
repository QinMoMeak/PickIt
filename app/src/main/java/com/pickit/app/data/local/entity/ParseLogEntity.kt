package com.pickit.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parse_logs")
data class ParseLogEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "request_type") val requestType: String,
    @ColumnInfo(name = "input_summary") val inputSummary: String? = null,
    val success: Boolean,
    val confidence: Float? = null,
    @ColumnInfo(name = "error_message") val errorMessage: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: String,
)
