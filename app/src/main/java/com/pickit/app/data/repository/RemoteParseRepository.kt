package com.pickit.app.data.repository

import android.content.Context
import android.net.Uri
import com.pickit.app.data.local.dao.ParseLogDao
import com.pickit.app.data.local.entity.ParseLogEntity
import com.pickit.app.data.preferences.SettingsPreferencesDataSource
import com.pickit.app.data.remote.api.ParseApiService
import com.pickit.app.data.remote.dto.ParseProductResponseDto
import com.pickit.app.domain.model.ParsedProductDraft
import com.pickit.app.domain.model.Platform
import com.pickit.app.domain.repository.ParseRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class RemoteParseRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val parseApiService: ParseApiService,
    private val settingsPreferencesDataSource: SettingsPreferencesDataSource,
    private val parseLogDao: ParseLogDao,
    private val json: Json,
) : ParseRepository {
    override suspend fun parseProduct(imageUri: Uri?, note: String?): Result<ParsedProductDraft> = runCatching {
        requireNotNull(imageUri) { "请先选择一张商品图片" }

        val settings = settingsPreferencesDataSource.settingsFlow.first()
        val requestUrl = settings.apiBaseUrl.trimEnd('/') + "/api/v1/parse-product"
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(imageUri) ?: "image/jpeg"
        val bytes = contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
            ?: error("无法读取选中的图片")

        val imageBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData(
            name = "image",
            filename = "pickit-upload.jpg",
            body = imageBody,
        )
        val noteBody = note
            ?.takeIf { it.isNotBlank() }
            ?.toRequestBody("text/plain".toMediaType())

        val response = parseApiService.parseProduct(
            url = requestUrl,
            image = imagePart,
            note = noteBody,
        )
        require(response.isSuccessful) {
            "识别接口请求失败，HTTP ${response.code()}"
        }

        val body = response.body()?.string().orEmpty()
        require(body.isNotBlank()) { "识别接口返回了空响应" }

        val dto = json.decodeFromString(ParseProductResponseDto.serializer(), body)
        val payload = requireNotNull(dto.data) { "识别接口没有返回商品数据" }

        ParsedProductDraft(
            title = payload.title.orEmpty(),
            brand = payload.brand,
            category = payload.category,
            subCategory = payload.subCategory,
            platform = payload.platform.toPlatform(),
            shopName = payload.shopName,
            priceText = payload.priceText,
            priceValue = payload.priceValue,
            currency = payload.currency ?: "CNY",
            spec = payload.spec,
            summary = payload.summary,
            recommendationReason = payload.recommendationReason,
            tags = payload.tags,
            confidence = payload.confidence,
            rawText = payload.rawText,
            sourceNote = note,
        ).also { draft ->
            parseLogDao.insert(
                ParseLogEntity(
                    id = UUID.randomUUID().toString(),
                    requestType = "IMAGE_PARSE",
                    inputSummary = note,
                    success = true,
                    confidence = draft.confidence,
                    errorMessage = null,
                    createdAt = Instant.now().toString(),
                ),
            )
        }
    }.onFailure { error ->
        parseLogDao.insert(
            ParseLogEntity(
                id = UUID.randomUUID().toString(),
                requestType = "IMAGE_PARSE",
                inputSummary = note,
                success = false,
                confidence = null,
                errorMessage = error.message,
                createdAt = Instant.now().toString(),
            ),
        )
    }

    private fun String?.toPlatform(): Platform = this?.let { raw ->
        Platform.entries.firstOrNull { it.name == raw }
    } ?: Platform.OTHER
}
