# AI 联调说明

## 安全约束

- 不要把 API Key 写入源码、常量、日志或提交记录
- API Key 只应通过 `local.properties`、环境变量或运行时设置页输入
- 日志中只允许输出脱敏后的 key

## Base URL 规则

设置页中应保存服务根路径，而不是完整接口路径。

推荐：

```text
https://open.bigmodel.cn/api/paas/v4/
```

实际请求时由代码统一拼接：

```text
chat/completions
```

## 最小可运行调用示例

### 1. Provider 侧最小 HTTP 请求示例

```kotlin
val endpoint = "${baseUrl.trimEnd('/')}/chat/completions"
val body = """
{
  "model": "glm-4.6v-flash",
  "messages": [
    {
      "role": "system",
      "content": [
        {
          "type": "text",
          "text": "你是一个商品截图结构化提取助手，只返回 JSON。"
        }
      ]
    },
    {
      "role": "user",
      "content": [
        {
          "type": "image_url",
          "image_url": {
            "url": "纯 base64 字符串，不带 data:image 前缀"
          }
        },
        {
          "type": "text",
          "text": "请识别商品名、平台、店铺、价格、标签，并严格返回 JSON。"
        }
      ]
    }
  ]
}
""".trimIndent()
```

注意：当前智谱 `glm-4.6v-flash` 实测对 `data:image/...;base64,...` 兼容性不好，使用纯 base64 字符串更稳定。

### 2. Android 端最小调用示例

```kotlin
viewModelScope.launch {
    val result = parseProductUseCase(
        imageUri = imageUri,
        userNote = "优先提取平台、价格和规格",
    )

    result.onSuccess { parsed ->
        _uiState.update { it.copy(errorMessage = null, draft = parsed.toDraft()) }
    }.onFailure { error ->
        _uiState.update { it.copy(errorMessage = error.toUserReadableMessage()) }
    }
}
```

## 当前错误摘要映射

- `ProviderConfigurationException` -> `AI 配置无效，请检查服务商、模型和地址`
- `AuthenticationFailureException` -> `鉴权失败，请检查 API Key`
- `RequestFormatException` -> `AI 请求格式错误`
- `MediaAccessException` / `ImageEncodingException` -> `图片编码失败或格式不支持`
- `NetworkRequestException` -> `AI 服务连接失败，请稍后重试`
- `EmptyModelResponseException` -> `模型返回为空`
- `ModelNonJsonResponseException` / `ModelJsonParseException` -> `模型返回内容无法解析`
- `NoProductInfoException` -> `当前图片未识别出有效商品信息`

## 当前 Bottom Sheet 实现

- 入口卡片：`AI 设置`
- 弹层组件：`ModalBottomSheet`
- 服务商：`FlowRow` 网格按钮
- 模型：`ExposedDropdownMenuBox`
- API Key：密码输入框 + 显示/隐藏
- Base URL：默认填服务根路径，保存时自动归一化

## 当前代码落点

- `app/src/main/java/com/pickit/app/application/ParseProductUseCase.kt`
- `app/src/main/java/com/pickit/app/infrastructure/ai/provider/zhipu/ZhipuVisionClient.kt`
- `app/src/main/java/com/pickit/app/infrastructure/ai/provider/zhipu/ZhipuResponseParser.kt`
- `app/src/main/java/com/pickit/app/infrastructure/ai/parser/StructuredJsonParser.kt`
- `app/src/main/java/com/pickit/app/presentation/ui/settings/SettingsScreen.kt`
- `app/src/main/java/com/pickit/app/presentation/ui/settings/SettingsViewModel.kt`
- `app/src/main/java/com/pickit/app/presentation/ui/preview/PreviewViewModel.kt`
