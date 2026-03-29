package com.pickit.app.infrastructure.ai.provider.openai_compatible

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class OpenAiCompatibleChatCompletionRequest(
    val model: String,
    val messages: List<OpenAiCompatibleMessage>,
    val temperature: Double? = null,
    @SerialName("max_tokens") val maxTokens: Int? = null,
    val stream: Boolean = false,
)

@Serializable
data class OpenAiCompatibleMessage(
    val role: String,
    val content: JsonElement,
)

@Serializable
data class OpenAiCompatibleChatCompletionResponse(
    val id: String? = null,
    val model: String? = null,
    val choices: List<OpenAiCompatibleChoice> = emptyList(),
)

@Serializable
data class OpenAiCompatibleChoice(
    val index: Int? = null,
    val message: OpenAiCompatibleAssistantMessage? = null,
    @SerialName("finish_reason") val finishReason: String? = null,
)

@Serializable
data class OpenAiCompatibleAssistantMessage(
    val role: String? = null,
    val content: JsonElement? = null,
)
