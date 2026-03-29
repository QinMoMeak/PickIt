package com.pickit.app.infrastructure.ai.config

import com.pickit.app.data.preferences.SettingsPreferencesDataSource
import com.pickit.app.domain.model.ModelProvider
import com.pickit.app.domain.model.ModelProviderConfig
import com.pickit.app.infrastructure.ai.error.ProviderConfigurationException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class ModelProviderConfigResolver @Inject constructor(
    private val settingsPreferencesDataSource: SettingsPreferencesDataSource,
) {
    suspend fun resolve(): ModelProviderConfig {
        val settings = settingsPreferencesDataSource.settingsFlow.first()
        val provider = settings.aiProvider.toModelProvider()
        val apiKey = settings.aiApiKey.trim()
        if (apiKey.isBlank()) {
            throw ProviderConfigurationException("请先配置 AI API Key")
        }

        return ModelProviderConfig(
            provider = provider,
            baseUrl = settings.apiBaseUrl.trim(),
            apiKey = apiKey,
            model = settings.aiModel.trim(),
            timeoutSeconds = settings.aiTimeoutSeconds.toLong(),
            enabledThinking = settings.aiEnableThinking,
            maxTokens = settings.aiMaxTokens,
            temperature = settings.aiTemperature,
        )
    }

    private fun String.toModelProvider(): ModelProvider = when (trim().uppercase()) {
        "ZHIPU" -> ModelProvider.ZHIPU
        "OPENAI_COMPATIBLE" -> ModelProvider.OPENAI_COMPATIBLE
        "OTHER" -> ModelProvider.OTHER
        else -> throw ProviderConfigurationException("不支持的 AI_PROVIDER: $this")
    }
}
