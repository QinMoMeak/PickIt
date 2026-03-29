package com.pickit.app.infrastructure.ai.provider.zhipu

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ZhipuChatCompletionRequest(
    val model: String,
    val messages: List<ZhipuMessage>,
    val temperature: Double? = null,
    @SerialName("max_tokens") val maxTokens: Int? = null,
    val thinking: ZhipuThinking? = null,
    val stream: Boolean = false,
)

@Serializable
data class ZhipuThinking(
    val type: String,
)

@Serializable
data class ZhipuMessage(
    val role: String,
    val content: JsonElement,
)

@Serializable
data class ZhipuChatCompletionResponse(
    val id: String? = null,
    @SerialName("request_id") val requestId: String? = null,
    val model: String? = null,
    val choices: List<ZhipuChoice> = emptyList(),
)

@Serializable
data class ZhipuChoice(
    val index: Int? = null,
    val message: ZhipuAssistantMessage? = null,
    @SerialName("finish_reason") val finishReason: String? = null,
)

@Serializable
data class ZhipuAssistantMessage(
    val role: String? = null,
    val content: JsonElement? = null,
    @SerialName("reasoning_content") val reasoningContent: String? = null,
)
