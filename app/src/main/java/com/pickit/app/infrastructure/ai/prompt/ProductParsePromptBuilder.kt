package com.pickit.app.infrastructure.ai.prompt

import com.pickit.app.domain.model.Platform
import com.pickit.app.domain.model.VisionParseRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductParsePromptBuilder @Inject constructor() {
    fun buildSystemPrompt(): String = """
        你是一个商品截图结构化提取助手。
        请根据输入图片和补充说明，识别并提取商品相关信息。
        只返回 JSON，不要输出 markdown，不要输出额外解释。
        如果无法确定字段，请返回 null。
        platform 只能是：
        DOUYIN, ALIBABA_1688, JD, TAOBAO, MEITUAN, PINDUODUO, OTHER
        confidence 取值范围 0 到 1。
        tags 必须是字符串数组。
        priceValue 必须是数字或 null。
        currency 缺失时返回 "CNY"。
    """.trimIndent()

    fun buildUserPrompt(request: VisionParseRequest): String {
        val preferredPlatforms = request.preferredPlatformCandidates
            .takeIf { it.isNotEmpty() }
            ?.joinToString(", ") { it.name }
            ?: "无"

        val userNote = request.userNote?.takeIf { it.isNotBlank() } ?: "无"

        return """
            请识别这张商品相关图片中的结构化信息，用于“好物收藏 App”。

            当前场景：${request.parseScene.name}
            结构化输出版本：${request.structuredOutputSchemaVersion}
            候选平台偏好：$preferredPlatforms
            用户补充说明：$userNote

            请尽量提取：
            - title
            - brand
            - category
            - subCategory
            - platform
            - shopName
            - priceText
            - priceValue
            - currency
            - spec
            - summary
            - recommendationReason
            - tags
            - confidence
            - rawText

            请严格返回以下 JSON 结构：
            {
              "title": null,
              "brand": null,
              "category": null,
              "subCategory": null,
              "platform": "OTHER",
              "shopName": null,
              "priceText": null,
              "priceValue": null,
              "currency": "CNY",
              "spec": null,
              "summary": null,
              "recommendationReason": null,
              "tags": [],
              "confidence": 0.0,
              "rawText": null
            }
        """.trimIndent()
    }
}
