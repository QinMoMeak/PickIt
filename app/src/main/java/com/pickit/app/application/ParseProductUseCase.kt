package com.pickit.app.application

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.pickit.app.domain.model.ParseScene
import com.pickit.app.domain.model.ParsedProductResult
import com.pickit.app.domain.model.Platform
import com.pickit.app.domain.model.VisionParseRequest
import com.pickit.app.domain.service.VisionParseService
import com.pickit.app.infrastructure.ai.error.ImageEncodingException
import com.pickit.app.infrastructure.ai.error.MediaAccessException
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ParseProductUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val visionParseService: VisionParseService,
) {
    suspend operator fun invoke(
        imageUri: Uri?,
        userNote: String?,
        preferredPlatformCandidates: List<Platform> = emptyList(),
    ): Result<ParsedProductResult> = runCatching {
        requireNotNull(imageUri) { "请先选择一张商品图片" }

        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(imageUri)
            ?.takeIf { it == "image/jpeg" || it == "image/png" || it == "image/webp" }
            ?: "image/jpeg"

        val bytes = runCatching {
            contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
        }.getOrElse { error ->
            throw MediaAccessException("无法读取选中的图片", error)
        } ?: throw MediaAccessException("无法读取选中的图片")

        if (bytes.isEmpty()) {
            throw ImageEncodingException("图片编码失败：读取到空数据")
        }

        val base64 = runCatching {
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        }.getOrElse { error ->
            throw ImageEncodingException("图片编码失败", error)
        }

        visionParseService.parseProduct(
            VisionParseRequest(
                imageBase64 = base64,
                userNote = userNote,
                parseScene = ParseScene.PRODUCT_RECOGNITION,
                preferredPlatformCandidates = preferredPlatformCandidates,
                structuredOutputSchemaVersion = 1,
            ),
        ).getOrThrow()
    }
}
