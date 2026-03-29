# AI Provider 接入架构

## 1. Provider 抽象设计

- 业务层统一依赖 `VisionParseService`
- 应用层通过 `ParseProductUseCase` 发起商品图片解析
- 基础设施层通过 `ModelProviderFactory` 选择具体服务商适配器
- 当前默认实现为 `ZhipuVisionClient`
- 已预留 `OpenAiCompatibleVisionClient`

注意：智谱当前使用 chat completions 风格接口与 `model=glm-4.6v-flash`，但这个格式只允许存在于智谱适配层中，不能污染上层业务代码。

## 2. 配置类

核心配置模型：`ModelProviderConfig`

字段：

- `provider`
- `baseUrl`
- `apiKey`
- `model`
- `timeoutSeconds`
- `enabledThinking`
- `maxTokens`
- `temperature`

默认值来源：

- Gradle `BuildConfig`
- 环境变量或 Gradle 属性：
  - `AI_PROVIDER`
  - `AI_BASE_URL`
  - `AI_API_KEY`
  - `AI_MODEL`
  - `AI_TIMEOUT_SECONDS`
  - `AI_ENABLE_THINKING`
  - `AI_MAX_TOKENS`
  - `AI_TEMPERATURE`

运行时覆盖：

- `SettingsPreferencesDataSource`

## 3. 统一请求/响应模型

请求：`VisionParseRequest`

- `imageUrl`
- `imageBase64`
- `userNote`
- `parseScene`
- `preferredPlatformCandidates`
- `structuredOutputSchemaVersion`

响应：`ParsedProductResult`

- `title`
- `brand`
- `category`
- `subCategory`
- `platform`
- `shopName`
- `priceText`
- `priceValue`
- `currency`
- `spec`
- `summary`
- `recommendationReason`
- `tags`
- `confidence`
- `rawText`
- `rawModelResponse`
- `provider`
- `model`

## 4. ZhipuVisionClient

当前默认 provider：智谱

- 端点：`POST /chat/completions`
- 默认基地址：`https://open.bigmodel.cn/api/paas/v4`
- 默认模型：`glm-4.6v-flash`
- 鉴权：`Authorization: Bearer {API_KEY}`
- 图像输入：`messages[].content` 中的 `image_url`
- 文本输入：`messages[].content` 中的 `text`

实现特性：

- 每次调用按配置动态选择模型和超时
- 失败时最多重试 1 次
- 区分配置错误、网络错误、HTTP 错误、非 JSON、JSON 解析错误
- 日志中对 API Key 做脱敏
- 保留原始响应文本到 `rawModelResponse`

## 5. Prompt 模板

系统提示词核心要求：

- 只返回 JSON
- 不输出 markdown
- 缺失字段返回 `null`
- `tags` 必须是字符串数组
- `confidence` 范围 0 到 1
- `platform` 只能是：
  - `DOUYIN`
  - `ALIBABA_1688`
  - `JD`
  - `TAOBAO`
  - `MEITUAN`
  - `PINDUODUO`
  - `OTHER`

用户提示词会补充：

- 当前解析场景
- schema 版本
- 用户备注
- 平台候选偏好
- 严格 JSON 模板

## 6. JSON 解析器

`StructuredJsonParser` 负责：

- 先尝试直接解析 JSON
- 若模型返回 fenced code block 或前后夹杂解释文本，则自动清洗
- 从首个 `{` 到最后一个 `}` 提取 JSON 对象
- 失败时抛出明确异常

`ZhipuResponseParser` 负责：

- 从智谱 `choices[0].message.content` 中提取文本
- 映射到统一 `ParsedProductResult`
- 标记 `provider=zhipu`
- 标记真实 `model`

## 7. ParseProductUseCase 对接示例

```kotlin
@HiltViewModel
class PreviewViewModel @Inject constructor(
    private val parseProductUseCase: ParseProductUseCase,
) : ViewModel() {
    fun parse(imageUri: Uri, note: String?) {
        viewModelScope.launch {
            val result = parseProductUseCase(
                imageUri = imageUri,
                userNote = note,
            )
        }
    }
}
```

## 8. 最小调用示例

```kotlin
suspend fun demo(useCase: ParseProductUseCase, imageUri: Uri) {
    val result = useCase(
        imageUri = imageUri,
        userNote = "重点识别平台、价格和规格",
    )

    result.onSuccess { parsed ->
        println(parsed.title)
        println(parsed.provider)
        println(parsed.model)
    }.onFailure { error ->
        println(error.message)
    }
}
```

## 当前代码落点

- 应用层：`app/src/main/java/com/pickit/app/application/ParseProductUseCase.kt`
- 领域接口：`app/src/main/java/com/pickit/app/domain/service/VisionParseService.kt`
- 统一模型：`app/src/main/java/com/pickit/app/domain/model/VisionParseModels.kt`
- 配置解析：`app/src/main/java/com/pickit/app/infrastructure/ai/config/ModelProviderConfigResolver.kt`
- 工厂：`app/src/main/java/com/pickit/app/infrastructure/ai/factory/ModelProviderFactory.kt`
- 智谱适配器：`app/src/main/java/com/pickit/app/infrastructure/ai/provider/zhipu/ZhipuVisionClient.kt`
- Prompt：`app/src/main/java/com/pickit/app/infrastructure/ai/prompt/ProductParsePromptBuilder.kt`
- JSON 解析：`app/src/main/java/com/pickit/app/infrastructure/ai/parser/StructuredJsonParser.kt`
