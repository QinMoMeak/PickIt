package com.pickit.app.presentation.ui.settings

data class AiProviderUiModel(
    val providerId: String,
    val displayName: String,
    val defaultBaseUrl: String,
    val supportedModels: List<String>,
)

object AiProviderCatalog {
    val providers: List<AiProviderUiModel> = listOf(
        AiProviderUiModel(
            providerId = "openai",
            displayName = "OpenAI",
            defaultBaseUrl = "https://api.openai.com/v1/chat/completions",
            supportedModels = listOf(
                "gpt-4.1-mini",
                "gpt-4o-mini",
            ),
        ),
        AiProviderUiModel(
            providerId = "gemini",
            displayName = "Google Gemini",
            defaultBaseUrl = "https://generativelanguage.googleapis.com/v1beta/openai/chat/completions",
            supportedModels = listOf(
                "gemini-2.5-flash",
                "gemini-1.5-pro",
            ),
        ),
        AiProviderUiModel(
            providerId = "anthropic",
            displayName = "Anthropic",
            defaultBaseUrl = "https://api.anthropic.com/v1/messages",
            supportedModels = listOf(
                "claude-3-5-sonnet-latest",
                "claude-3-5-haiku-latest",
            ),
        ),
        AiProviderUiModel(
            providerId = "deepseek",
            displayName = "DeepSeek",
            defaultBaseUrl = "https://api.deepseek.com/chat/completions",
            supportedModels = listOf(
                "deepseek-chat",
                "deepseek-reasoner",
            ),
        ),
        AiProviderUiModel(
            providerId = "qwen",
            displayName = "Qwen",
            defaultBaseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions",
            supportedModels = listOf(
                "qwen2.5-vl-72b-instruct",
                "qwen2.5-72b-instruct",
            ),
        ),
        AiProviderUiModel(
            providerId = "zhipu",
            displayName = "Z.ai",
            defaultBaseUrl = "https://open.bigmodel.cn/api/paas/v4/chat/completions",
            supportedModels = listOf(
                "glm-4.6v-flash",
                "glm-4.5v",
                "glm-4-flash",
            ),
        ),
        AiProviderUiModel(
            providerId = "doubao",
            displayName = "Doubao",
            defaultBaseUrl = "https://ark.cn-beijing.volces.com/api/v3/chat/completions",
            supportedModels = listOf(
                "doubao-1.5-vision-pro-32k",
                "doubao-1.5-pro-32k",
            ),
        ),
    )

    fun find(providerId: String): AiProviderUiModel =
        providers.firstOrNull { it.providerId.equals(providerId.trim(), ignoreCase = true) }
            ?: providers.first { it.providerId == "zhipu" }
}
