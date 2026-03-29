下面直接给你一版 **可交给 Codex 的安卓实施文档**。

------

# AI 商品收藏 App 实施文档

## 1. 产品目标

开发一个 Android App，用于收藏和整理日常刷到的商品推荐信息。
用户主要输入是 **图片**，也支持补充文字。App 将图片或图文发送到后端，由后端调用大模型 API 分析，并返回结构化商品信息，供用户确认后保存。

约束：过程中做好工作日志维护，上下文过长时可以查询；全程中文使用utf8格式；不用询问我意见，直接做到全面完成；中文名： 拾物 英文名： PickIt ；ui设计使用skills：UI-UX-Pro-Max

核心能力：

- 上传商品相关图片
- AI 识别商品信息
- 自动提取平台、店铺、价格、商品名、标签等
- 支持价格历史
- 支持模糊搜索
- 支持本地导入导出
- 支持 WebDAV 同步/备份
- 不做账号系统
- 不做网站迁移

------

## 2. 已确认需求

- **架构**：Android + 后端
- **常见输入**：图片为主
- **常见平台**：
  - 抖音
  - 1688
  - 京东
  - 淘宝
  - 美团
  - 拼多多
- **不保存原始图**
- **要记录价格历史**
- **状态字段**至少包括：
  - 未购买
  - 正在使用
  - 使用完
  - 已损坏
- **同步方式**：
  - WebDAV
  - 本地导入导出
- **不需要账号登录**
- **不需要以后迁移网站**

------

## 3. 产品定位

这是一个：

**AI 商品灵感收藏与待购管理 App**

重点不是“手工记账式录入”，而是：

- 快速采集
- AI 自动整理
- 后续可搜索、可筛选、可回看
- 适合“看见好物先存起来，以后再买”

------

## 4. 推荐技术架构

## 4.1 Android 客户端

- Kotlin
- Jetpack Compose
- MVVM
- Room
- DataStore
- Hilt
- Coil
- WorkManager
- kotlinx.serialization
- OkHttp / Retrofit

## 4.2 后端

建议做轻量后端，不让 App 直接调用大模型。

推荐：

- Java Spring Boot
  或
- Node.js NestJS / Express

后端职责：

- 接收 App 上传的图片和文字
- 调用 OCR / 多模态大模型
- 返回结构化 JSON
- 统一 Prompt 和解析规则
- 控制 API Key、安全、限流、重试

------

## 5. 整体流程

## 5.1 图片录入主流程

1. 用户选择一张商品相关图片
2. App 本地压缩图片
3. 上传给后端解析接口
4. 后端调用 OCR / 多模态模型
5. 后端返回结构化商品信息
6. App 进入“识别预览页”
7. 用户修改、补充、确认
8. 保存到本地数据库
9. 生成价格历史记录
10. 可选同步到 WebDAV 备份

------

## 5.2 图文联合录入流程

1. 用户上传图片
2. 补充一句说明文字
3. App 将图片 + 文本一起发给后端
4. 后端综合识别
5. 返回结构化结果
6. 用户确认保存

------

## 5.3 后续搜索流程

1. 用户输入模糊关键词
2. 本地搜索商品表和标签表
3. 按标题、平台、店铺、标签、摘要、状态匹配
4. 返回结果列表
5. 点击进入详情页查看价格历史和备注

------

## 6. Android 页面结构

## 6.1 首页

展示商品收藏列表。

功能：

- 搜索框
- 平台筛选
- 状态筛选
- 标签筛选
- 最近新增
- 新增按钮

## 6.2 新增页

录入入口：

- 拍照 / 相册选图
- 补充文字说明
- 开始 AI 识别

## 6.3 识别预览页

展示 AI 返回结果：

- 商品名
- 品牌
- 店铺
- 平台
- 价格
- 标签
- 推荐摘要
- 识别置信度

支持：

- 修改字段
- 删除错误标签
- 增加备注
- 确认保存

## 6.4 详情页

展示完整商品信息：

- 商品基本信息
- 当前状态
- 标签
- 店铺与平台
- 推荐摘要
- 价格历史
- 来源说明
- 编辑入口

## 6.5 统计页

展示：

- 总记录数
- 各平台数量
- 各状态数量
- 常见标签 Top N
- 最近新增数量
- 最近价格波动记录数

## 6.6 设置页

配置：

- 后端 API 地址
- WebDAV 配置
- 导入导出
- 图片压缩设置
- 同步设置
- 数据清理

------

## 7. 项目结构建议

```text
app/
  src/main/java/com/example/goodstash/
    App.kt
    MainActivity.kt

    core/
      common/
      network/
      image/
      json/
      webdav/
      search/
      util/

    data/
      local/
        db/
        dao/
        entity/
        relation/
      remote/
        api/
        dto/
      repository/
      mapper/

    domain/
      model/
      repository/
      usecase/

    presentation/
      navigation/
      ui/
        home/
        add/
        preview/
        detail/
        stats/
        settings/
      component/
      theme/

    di/
```

------

## 8. 数据库设计

## 8.1 商品主表 `product_items`

```sql
CREATE TABLE product_items (
  id TEXT PRIMARY KEY NOT NULL,
  title TEXT NOT NULL,
  brand TEXT,
  category TEXT,
  sub_category TEXT,
  platform TEXT,
  shop_name TEXT,
  current_price_text TEXT,
  current_price_value REAL,
  currency TEXT NOT NULL DEFAULT 'CNY',
  spec TEXT,
  summary TEXT,
  recommendation_reason TEXT,
  status TEXT NOT NULL DEFAULT 'NOT_PURCHASED',
  source_type TEXT NOT NULL DEFAULT 'IMAGE',
  source_note TEXT,
  confidence REAL,
  ai_raw_json TEXT,
  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL
);
```

------

## 8.2 标签表 `tags`

```sql
CREATE TABLE tags (
  id TEXT PRIMARY KEY NOT NULL,
  name TEXT NOT NULL,
  normalized_name TEXT NOT NULL UNIQUE,
  created_at TEXT NOT NULL
);
```

------

## 8.3 商品标签关联表 `product_tag_ref`

```sql
CREATE TABLE product_tag_ref (
  product_id TEXT NOT NULL,
  tag_id TEXT NOT NULL,
  PRIMARY KEY (product_id, tag_id),
  FOREIGN KEY(product_id) REFERENCES product_items(id) ON DELETE CASCADE,
  FOREIGN KEY(tag_id) REFERENCES tags(id) ON DELETE CASCADE
);
```

------

## 8.4 价格历史表 `price_history`

```sql
CREATE TABLE price_history (
  id TEXT PRIMARY KEY NOT NULL,
  product_id TEXT NOT NULL,
  price_text TEXT,
  price_value REAL,
  currency TEXT NOT NULL DEFAULT 'CNY',
  platform TEXT,
  shop_name TEXT,
  source_note TEXT,
  recorded_at TEXT NOT NULL,
  FOREIGN KEY(product_id) REFERENCES product_items(id) ON DELETE CASCADE
);
```

说明：

- 每次新建商品时，自动插入一条价格历史
- 后续编辑价格时也插入新记录
- 不覆盖历史价格

------

## 8.5 解析记录表 `parse_logs`

```sql
CREATE TABLE parse_logs (
  id TEXT PRIMARY KEY NOT NULL,
  request_type TEXT NOT NULL,
  input_summary TEXT,
  success INTEGER NOT NULL,
  confidence REAL,
  error_message TEXT,
  created_at TEXT NOT NULL
);
```

用于排查 AI 解析问题。

------

## 9. Domain Model

```kotlin
data class ProductItem(
    val id: String,
    val title: String,
    val brand: String?,
    val category: String?,
    val subCategory: String?,
    val platform: Platform?,
    val shopName: String?,
    val currentPriceText: String?,
    val currentPriceValue: Double?,
    val currency: String = "CNY",
    val spec: String?,
    val summary: String?,
    val recommendationReason: String?,
    val tags: List<Tag>,
    val status: ProductStatus,
    val sourceType: SourceType,
    val sourceNote: String?,
    val confidence: Float?,
    val aiRawJson: String?,
    val createdAt: String,
    val updatedAt: String
)

data class Tag(
    val id: String,
    val name: String
)

data class PriceHistory(
    val id: String,
    val productId: String,
    val priceText: String?,
    val priceValue: Double?,
    val currency: String,
    val platform: Platform?,
    val shopName: String?,
    val sourceNote: String?,
    val recordedAt: String
)

enum class ProductStatus {
    NOT_PURCHASED,
    IN_USE,
    USED_UP,
    DAMAGED,
    ABANDONED
}

enum class SourceType {
    IMAGE,
    IMAGE_WITH_TEXT,
    TEXT
}

enum class Platform {
    DOUYIN,
    ALIBABA_1688,
    JD,
    TAOBAO,
    MEITUAN,
    PINDUODUO,
    OTHER
}
```

------

## 10. AI 解析返回 JSON 规范

后端必须返回稳定 JSON，不要返回自由文本。

```json
{
  "title": "冻干苹果脆",
  "brand": "某品牌",
  "category": "零食",
  "sub_category": "果干",
  "platform": "TAOBAO",
  "shop_name": "某某旗舰店",
  "price_text": "29.9元两袋",
  "price_value": 29.9,
  "currency": "CNY",
  "spec": "2袋",
  "summary": "适合作为办公室低负担零食",
  "recommendation_reason": "配料简单，适合囤货",
  "tags": ["零食", "水果干", "办公室", "回购候选"],
  "confidence": 0.87
}
```

### 解析规则

- 缺失字段可返回 `null`
- `platform` 必须标准化
- `tags` 必须是字符串数组
- `confidence` 范围 0~1
- `price_value` 尽量转数值
- `price_text` 保留原始表达

------

## 11. 后端接口设计

## 11.1 解析商品接口

```
POST /api/v1/parse-product
```

请求：

- `multipart/form-data`

字段：

- `image`: 图片文件
- `note`: 可选文字说明

返回：

```json
{
  "success": true,
  "data": {
    "title": "苹果脆",
    "brand": "某品牌",
    "category": "零食",
    "sub_category": "果干",
    "platform": "TAOBAO",
    "shop_name": "旗舰店",
    "price_text": "29.9元",
    "price_value": 29.9,
    "currency": "CNY",
    "spec": "500g",
    "summary": "适合办公室日常囤货",
    "recommendation_reason": "配料干净",
    "tags": ["零食", "果干"],
    "confidence": 0.83,
    "raw_text": "OCR或模型提取的中间文本"
  }
}
```

------

## 11.2 健康检查接口

```
GET /api/v1/health
```

返回后端是否可用。

------

## 11.3 模型配置接口

可选。

```
GET /api/v1/models
```

用于查看当前启用模型。

------

## 12. Android Repository 设计

```kotlin
interface ProductRepository {
    fun observeProducts(): Flow<List<ProductItem>>
    suspend fun getProduct(id: String): ProductItem?
    suspend fun saveProduct(product: ProductItem)
    suspend fun updateProduct(product: ProductItem)
    suspend fun deleteProduct(id: String)
    suspend fun search(query: String, filter: ProductFilter): List<ProductItem>
}

interface ParseRepository {
    suspend fun parseProduct(imageUri: Uri, note: String?): Result<ParsedProductDraft>
}

interface PriceHistoryRepository {
    suspend fun addPriceHistory(history: PriceHistory)
    suspend fun getPriceHistory(productId: String): List<PriceHistory>
}

interface SyncRepository {
    suspend fun exportJson(uri: Uri): Result<Unit>
    suspend fun importJson(uri: Uri): Result<Unit>
    suspend fun backupToWebDav(): Result<Unit>
    suspend fun restoreFromWebDav(): Result<Unit>
}
```

------

## 13. 搜索设计

V1 用本地模糊搜索，不做向量检索。

### 搜索范围

- 商品名
- 品牌
- 店铺名
- 平台
- 标签
- 摘要
- 推荐理由
- 来源备注

### 推荐方案

- Room + FTS
- 标签表关联搜索
- 平台和状态条件筛选

### 支持筛选

- 平台筛选
- 状态筛选
- 标签筛选
- 价格区间筛选
- 最近新增
- 最近修改

------

## 14. 导入导出与 WebDAV

因为不需要账号，但需要跨设备，建议保持：

## 14.1 本地导出

支持：

- JSON 导出
- CSV 导出

### JSON

用于完整备份：

- 商品
- 标签
- 价格历史

### CSV

用于简单查看和表格处理

------

## 14.2 WebDAV

继续沿用你之前那套：

- 手动备份
- 手动恢复
- 可选自动同步任务

推荐目录结构：

```text
/goodstash/
  manifest.json
  backup-latest.json
  backup-latest.csv
  backups/
    backup-2026-03-28T10-00-00.json
    backup-2026-03-28T10-00-00.csv
```

------

## 15. 图片处理策略

你说 **不保存原始图**，所以设计要注意：

### 客户端

- 选择图片后先本地压缩
- 上传给后端
- 解析完成后不入库保存图片
- 仅保留：
  - 提取结果
  - OCR 文本摘要
  - 原始备注
  - AI 原始 JSON

### 好处

- 本地体积小
- 同步轻量
- 不涉及大量媒体文件

### 风险

- 后续无法重新查看原始截图
- 所以建议保留一个字段：
  - `source_note`
    用于记录“这张图是什么场景”

------

## 16. 状态流转建议

```text
未购买 -> 正在使用 -> 使用完
未购买 -> 已损坏
正在使用 -> 已损坏
任意状态 -> 已放弃
```

用于 UI 和统计。

------

## 17. 统计模块

首页外单独做统计页，展示：

- 总商品数
- 未购买数量
- 正在使用数量
- 使用完数量
- 已损坏数量
- 各平台数量
- 热门标签 Top N
- 最近 7 天新增
- 最近 30 天新增
- 有价格历史的商品数量

------

## 18. Android 页面路由建议

```kotlin
object Routes {
    const val Home = "home"
    const val Add = "add"
    const val Preview = "preview"
    const val Detail = "detail/{productId}"
    const val Stats = "stats"
    const val Settings = "settings"
    const val ImportExport = "import_export"
    const val WebDav = "webdav"
}
```

底部导航建议：

- 首页
- 统计
- 设置

------

## 19. 开发优先级

## P0

- 本地数据库
- 图片上传解析
- 识别预览页
- 商品保存
- 列表页
- 搜索页
- 详情页
- 价格历史
- JSON 导入导出
- WebDAV 备份恢复

## P1

- CSV 导出
- 高级筛选
- 平台统计
- 标签统计
- WorkManager 自动备份

## P2

- 批量导入图片
- 剪贴板识别
- 分享到 App
- 价格趋势图

------

## 20. 给 Codex 的直接开发指令

```text
请开发一个 Android App，名称暂定 GoodStash，用于收藏日常刷到的商品推荐信息。

技术要求：
- Kotlin
- Jetpack Compose
- MVVM
- Room
- Hilt
- DataStore
- Retrofit / OkHttp
- kotlinx.serialization
- Coil
- WorkManager

核心业务：
1. 用户以图片录入为主，可附加文字说明。
2. App 将图片和说明上传到后端解析接口。
3. 后端返回结构化商品 JSON。
4. App 展示识别预览页，允许用户修改并确认保存。
5. 商品信息保存到本地数据库。
6. 支持价格历史，不覆盖旧价格。
7. 支持模糊搜索和筛选。
8. 支持本地 JSON/CSV 导入导出。
9. 支持 WebDAV 备份恢复。
10. 不做账号系统，不保存原始图片。

数据字段包括：
- id
- title
- brand
- category
- subCategory
- platform
- shopName
- currentPriceText
- currentPriceValue
- currency
- spec
- summary
- recommendationReason
- tags
- status
- sourceType
- sourceNote
- confidence
- aiRawJson
- createdAt
- updatedAt

状态枚举：
- NOT_PURCHASED
- IN_USE
- USED_UP
- DAMAGED
- ABANDONED

平台枚举：
- DOUYIN
- ALIBABA_1688
- JD
- TAOBAO
- MEITUAN
- PINDUODUO
- OTHER

数据库表至少实现：
- product_items
- tags
- product_tag_ref
- price_history
- parse_logs

页面至少实现：
- 首页
- 新增页
- 识别预览页
- 详情页
- 统计页
- 设置页
- 导入导出页
- WebDAV 设置页

后端接口：
- POST /api/v1/parse-product
- GET /api/v1/health

请先输出：
1. 完整项目目录结构
2. Room entities / dao / database
3. domain model
4. repository interfaces and implementations skeleton
5. parse API dto 和 retrofit service
6. 首页、预览页、详情页、统计页 Compose 骨架
7. JSON 导入导出和 WebDAV 骨架
8. 最后确保 assembleDebug 可通过
```

------

## 21. 后端给 Codex 的指令

如果你后面还要让 Codex 搭后端，可以直接再发这段：

```text
请开发一个轻量后端服务，为 Android 商品收藏 App 提供 AI 解析接口。

要求：
1. 提供 POST /api/v1/parse-product
2. 接收 multipart/form-data：
   - image
   - note（可选）
3. 后端调用 OCR / 多模态大模型，对商品截图进行结构化提取
4. 返回 JSON：
   - title
   - brand
   - category
   - sub_category
   - platform
   - shop_name
   - price_text
   - price_value
   - currency
   - spec
   - summary
   - recommendation_reason
   - tags
   - confidence
   - raw_text
5. 提供 GET /api/v1/health
6. 增加统一异常处理、超时控制、日志记录
7. Prompt 必须强制模型返回结构化 JSON，不允许自由文本
8. 平台枚举支持：
   - DOUYIN
   - ALIBABA_1688
   - JD
   - TAOBAO
   - MEITUAN
   - PINDUODUO
   - OTHER
```

------

## 22. 结论

这版方案适合你当前习惯：

- **图片录入为主**
- **AI 自动提取**
- **本地保存为核心**
- **价格历史可追踪**
- **WebDAV 做多设备同步**
- **不依赖账号**
- **不存原图，保持轻量**

下一步最合适的是：
先让 Codex 按第 20 节把 **Android 客户端骨架** 搭出来，再单独搭第 21 节的后端。