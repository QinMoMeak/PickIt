package com.pickit.app.domain.repository

import android.net.Uri
import com.pickit.app.domain.model.ParsedProductDraft

interface ParseRepository {
    suspend fun parseProduct(imageUri: Uri?, note: String?): Result<ParsedProductDraft>
}
