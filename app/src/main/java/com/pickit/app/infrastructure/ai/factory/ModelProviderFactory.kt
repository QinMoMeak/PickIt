package com.pickit.app.infrastructure.ai.factory

import com.pickit.app.domain.model.ModelProvider
import com.pickit.app.infrastructure.ai.error.ProviderConfigurationException
import com.pickit.app.infrastructure.ai.provider.VisionProviderClient
import com.pickit.app.infrastructure.ai.provider.openai_compatible.OpenAiCompatibleVisionClient
import com.pickit.app.infrastructure.ai.provider.zhipu.ZhipuVisionClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelProviderFactory @Inject constructor(
    private val zhipuVisionClient: ZhipuVisionClient,
    private val openAiCompatibleVisionClient: OpenAiCompatibleVisionClient,
) {
    fun create(provider: ModelProvider): VisionProviderClient = when (provider) {
        ModelProvider.ZHIPU -> zhipuVisionClient
        ModelProvider.OPENAI_COMPATIBLE -> openAiCompatibleVisionClient
        ModelProvider.OTHER -> throw ProviderConfigurationException("OTHER provider 尚未实现具体适配器")
    }
}
