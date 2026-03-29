package com.pickit.app.infrastructure.ai.provider.openai_compatible

import com.pickit.app.domain.model.ModelProvider
import com.pickit.app.domain.model.ModelProviderConfig
import com.pickit.app.domain.model.ParsedProductResult
import com.pickit.app.domain.model.VisionParseRequest
import com.pickit.app.infrastructure.ai.error.ProviderConfigurationException
import com.pickit.app.infrastructure.ai.provider.VisionProviderClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenAiCompatibleVisionClient @Inject constructor() : VisionProviderClient {
    override val provider: ModelProvider = ModelProvider.OPENAI_COMPATIBLE

    override suspend fun parse(
        config: ModelProviderConfig,
        request: VisionParseRequest,
    ): Result<ParsedProductResult> = Result.failure(
        ProviderConfigurationException("OpenAI-compatible vision provider 预留完成，当前尚未实现具体调用"),
    )
}
