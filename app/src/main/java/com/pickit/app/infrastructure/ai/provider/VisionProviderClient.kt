package com.pickit.app.infrastructure.ai.provider

import com.pickit.app.domain.model.ModelProvider
import com.pickit.app.domain.model.ModelProviderConfig
import com.pickit.app.domain.model.ParsedProductResult
import com.pickit.app.domain.model.VisionParseRequest

interface VisionProviderClient {
    val provider: ModelProvider

    suspend fun parse(
        config: ModelProviderConfig,
        request: VisionParseRequest,
    ): Result<ParsedProductResult>
}
