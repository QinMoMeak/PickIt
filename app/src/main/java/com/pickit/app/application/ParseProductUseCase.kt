package com.pickit.app.application

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.pickit.app.domain.model.ParseScene
import com.pickit.app.domain.model.ParsedProductResult
import com.pickit.app.domain.model.Platform
import com.pickit.app.domain.model.VisionParseRequest
import com.pickit.app.domain.service.VisionParseService
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
        val mimeType = contentResolver.getType(imageUri) ?: "image/jpeg"
        val bytes = contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
            ?: error("无法读取选中的图片")
        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        val dataUri = "data:$mimeType;base64,$base64"

        visionParseService.parseProduct(
            VisionParseRequest(
                imageBase64 = dataUri,
                userNote = userNote,
                parseScene = ParseScene.PRODUCT_RECOGNITION,
                preferredPlatformCandidates = preferredPlatformCandidates,
                structuredOutputSchemaVersion = 1,
            ),
        ).getOrThrow()
    }
}
