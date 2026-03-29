package com.pickit.app.domain.repository

import android.net.Uri

interface SyncRepository {
    suspend fun exportJson(uri: Uri?): Result<Unit>
    suspend fun importJson(uri: Uri?): Result<Unit>
    suspend fun backupToWebDav(): Result<Unit>
    suspend fun restoreFromWebDav(): Result<Unit>
}
