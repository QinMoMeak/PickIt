package com.pickit.app.data.repository

import android.content.Context
import android.net.Uri
import com.pickit.app.BuildConfig
import com.pickit.app.data.local.dao.PriceHistoryDao
import com.pickit.app.data.local.dao.ProductDao
import com.pickit.app.data.local.dao.TagDao
import com.pickit.app.data.local.db.PickItDatabase
import com.pickit.app.data.local.entity.PriceHistoryEntity
import com.pickit.app.data.local.entity.ProductEntity
import com.pickit.app.data.local.entity.ProductTagCrossRef
import com.pickit.app.data.local.entity.TagEntity
import com.pickit.app.data.preferences.SettingsPreferences
import com.pickit.app.data.preferences.SettingsPreferencesDataSource
import com.pickit.app.domain.model.PriceHistory
import com.pickit.app.domain.model.ProductItem
import com.pickit.app.domain.model.Tag
import com.pickit.app.domain.repository.ProductRepository
import com.pickit.app.domain.repository.SyncRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import androidx.room.withTransaction
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

private const val WebDavBackupFileName = "pickit-backup.json"

@Singleton
class RoomSyncRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: PickItDatabase,
    private val productDao: ProductDao,
    private val tagDao: TagDao,
    private val priceHistoryDao: PriceHistoryDao,
    private val productRepository: ProductRepository,
    private val settingsPreferencesDataSource: SettingsPreferencesDataSource,
    private val okHttpClient: OkHttpClient,
    private val json: Json,
) : SyncRepository {
    override suspend fun exportJson(uri: Uri?): Result<Unit> = runCatching {
        requireNotNull(uri) { "请选择导出文件位置" }

        val payload = buildBackupPayload()
        val encoded = json.encodeToString(BackupPayload.serializer(), payload)
        context.contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use { writer ->
            writer.write(encoded)
        } ?: error("无法写入导出文件")
    }

    override suspend fun importJson(uri: Uri?): Result<Unit> = runCatching {
        requireNotNull(uri) { "请选择要导入的备份文件" }

        val content = context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { reader ->
            reader.readText()
        } ?: error("无法读取导入文件")

        importBackupPayload(content)
    }

    override suspend fun backupToWebDav(): Result<Unit> = runCatching {
        val payload = buildBackupPayload()
        val encoded = json.encodeToString(BackupPayload.serializer(), payload)
        val settings = settingsPreferencesDataSource.settingsFlow.first()
        val request = Request.Builder()
            .url(buildWebDavFileUrl(settings))
            .put(encoded.toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            require(response.isSuccessful) { "WebDAV 备份失败，HTTP ${response.code}" }
        }
    }

    override suspend fun restoreFromWebDav(): Result<Unit> = runCatching {
        val settings = settingsPreferencesDataSource.settingsFlow.first()
        val request = Request.Builder()
            .url(buildWebDavFileUrl(settings))
            .get()
            .build()

        val content = okHttpClient.newCall(request).execute().use { response ->
            require(response.isSuccessful) { "WebDAV 恢复失败，HTTP ${response.code}" }
            response.body?.string().orEmpty().ifBlank { error("WebDAV 返回了空备份") }
        }

        importBackupPayload(content)
    }

    private suspend fun buildBackupPayload(): BackupPayload {
        val products = productRepository.observeProducts().first()
        return BackupPayload(
            schemaVersion = 1,
            exportedAt = Instant.now().toString(),
            appVersion = BuildConfig.VERSION_NAME,
            products = products.map(::toBackupProduct),
        )
    }

    private suspend fun importBackupPayload(content: String) {
        val payload = json.decodeFromString(BackupPayload.serializer(), content)
        val productEntities = payload.products.map(::toProductEntity)
        val tagEntities = payload.products
            .flatMap { product -> product.tags.map(::toTagEntity) }
            .distinctBy { it.id }
        val refs = payload.products
            .flatMap { product ->
                product.tags.map { tag -> ProductTagCrossRef(product_id = product.id, tag_id = tag.id) }
            }
            .distinctBy { "${it.product_id}:${it.tag_id}" }
        val priceHistory = payload.products
            .flatMap { product ->
                product.priceHistory.map { history -> toPriceHistoryEntity(product.id, history) }
            }

        database.clearAllTables()
        database.withTransaction {
            productEntities.forEach { product -> productDao.insert(product) }
            if (tagEntities.isNotEmpty()) {
                tagDao.upsertTags(tagEntities)
            }
            if (refs.isNotEmpty()) {
                tagDao.upsertRefs(refs)
            }
            priceHistory.forEach { history -> priceHistoryDao.insert(history) }
        }
    }

    private fun buildWebDavFileUrl(settings: SettingsPreferences): String {
        val rawPath = settings.webDavPath.trim()
        if (rawPath.startsWith("http://") || rawPath.startsWith("https://")) {
            return if (rawPath.endsWith(".json")) rawPath else rawPath.trimEnd('/') + "/$WebDavBackupFileName"
        }

        val baseUrl = settings.apiBaseUrl.trim().trimEnd('/')
        require(baseUrl.isNotBlank()) { "请先配置 API 地址" }

        val normalizedPath = rawPath.trim('/').takeIf { it.isNotBlank() }
        return when {
            normalizedPath == null -> "$baseUrl/$WebDavBackupFileName"
            rawPath.endsWith(".json") -> "$baseUrl/$normalizedPath"
            else -> "$baseUrl/$normalizedPath/$WebDavBackupFileName"
        }
    }

    private fun toBackupProduct(product: ProductItem): BackupProduct = BackupProduct(
        id = product.id,
        title = product.title,
        brand = product.brand,
        category = product.category,
        subCategory = product.subCategory,
        platform = product.platform.name,
        shopName = product.shopName,
        currentPriceText = product.currentPriceText,
        currentPriceValue = product.currentPriceValue,
        currency = product.currency,
        spec = product.spec,
        summary = product.summary,
        recommendationReason = product.recommendationReason,
        tags = product.tags.map(::toBackupTag),
        status = product.status.name,
        sourceType = product.sourceType.name,
        sourceNote = product.sourceNote,
        confidence = product.confidence,
        aiRawJson = product.aiRawJson,
        priceHistory = product.priceHistory.map(::toBackupPriceHistory),
        createdAt = product.createdAt,
        updatedAt = product.updatedAt,
    )

    private fun toBackupTag(tag: Tag): BackupTag = BackupTag(
        id = tag.id,
        name = tag.name,
        normalizedName = tag.name.trim().lowercase(),
        createdAt = Instant.now().toString(),
    )

    private fun toBackupPriceHistory(priceHistory: PriceHistory): BackupPriceHistory = BackupPriceHistory(
        id = priceHistory.id,
        priceText = priceHistory.priceText,
        priceValue = priceHistory.priceValue,
        currency = priceHistory.currency,
        platform = priceHistory.platform.name,
        shopName = priceHistory.shopName,
        sourceNote = priceHistory.sourceNote,
        recordedAt = priceHistory.recordedAt,
    )

    private fun toProductEntity(product: BackupProduct): ProductEntity = ProductEntity(
        id = product.id,
        title = product.title,
        brand = product.brand,
        category = product.category,
        subCategory = product.subCategory,
        platform = product.platform,
        shopName = product.shopName,
        currentPriceText = product.currentPriceText,
        currentPriceValue = product.currentPriceValue,
        currency = product.currency,
        spec = product.spec,
        summary = product.summary,
        recommendationReason = product.recommendationReason,
        status = product.status,
        sourceType = product.sourceType,
        sourceNote = product.sourceNote,
        confidence = product.confidence,
        aiRawJson = product.aiRawJson,
        createdAt = product.createdAt,
        updatedAt = product.updatedAt,
    )

    private fun toTagEntity(tag: BackupTag): TagEntity = TagEntity(
        id = tag.id,
        name = tag.name,
        normalizedName = tag.normalizedName,
        createdAt = tag.createdAt,
    )

    private fun toPriceHistoryEntity(productId: String, history: BackupPriceHistory): PriceHistoryEntity =
        PriceHistoryEntity(
            id = history.id,
            productId = productId,
            priceText = history.priceText,
            priceValue = history.priceValue,
            currency = history.currency,
            platform = history.platform,
            shopName = history.shopName,
            sourceNote = history.sourceNote,
            recordedAt = history.recordedAt,
        )
}

@Serializable
private data class BackupPayload(
    val schemaVersion: Int,
    val exportedAt: String,
    val appVersion: String,
    val products: List<BackupProduct>,
)

@Serializable
private data class BackupProduct(
    val id: String,
    val title: String,
    val brand: String? = null,
    val category: String? = null,
    val subCategory: String? = null,
    val platform: String,
    val shopName: String? = null,
    val currentPriceText: String? = null,
    val currentPriceValue: Double? = null,
    val currency: String = "CNY",
    val spec: String? = null,
    val summary: String? = null,
    val recommendationReason: String? = null,
    val tags: List<BackupTag> = emptyList(),
    val status: String,
    val sourceType: String,
    val sourceNote: String? = null,
    val confidence: Float? = null,
    val aiRawJson: String? = null,
    val priceHistory: List<BackupPriceHistory> = emptyList(),
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
private data class BackupTag(
    val id: String,
    val name: String,
    val normalizedName: String,
    val createdAt: String,
)

@Serializable
private data class BackupPriceHistory(
    val id: String,
    val priceText: String? = null,
    val priceValue: Double? = null,
    val currency: String = "CNY",
    val platform: String? = null,
    val shopName: String? = null,
    val sourceNote: String? = null,
    val recordedAt: String,
)
