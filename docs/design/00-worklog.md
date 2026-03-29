# PickIt 设计与实现工作日志

## 2026-03-28

### 输入材料

- 通读 `docs/begin.md`
- 使用 `ui-ux-pro-max` 生成第一版设计系统与 Compose 交接资料
- 对照项目实际目标，剔除偏营销落地页的表达，收敛为工具型 App 设计

### 已确认的设计约束

- 全流程以中文为主，界面文案和信息结构优先服务中文语境
- 不保存原始图片，因此详情页不能依赖截图回看
- 首页必须优先强化搜索、筛选和回看效率
- AI 识别不是终点，预览页必须支持快速修正和确认保存
- WebDAV 与本地导入导出属于高风险操作，交互必须明确提示覆盖风险

### 关键决策

- 视觉方向调整为“工具型秩序感 + 温暖商品灵感感”
- 主色使用青绿，行动色使用橙色，避免贴近通用电商配色
- 页面结构优先信息密度与检索效率，不走大面积装饰型布局
- 底部导航固定为：首页、统计、设置；新增商品使用悬浮主按钮承载

### 已交付设计资产

- `docs/design/01-design-strategy.md`
- `docs/design/02-information-architecture.md`
- `docs/design/03-user-flows.md`
- `docs/design/04-screen-specs.md`
- `docs/design/05-visual-system.md`
- `docs/design/06-copy-and-states.md`
- `docs/design/07-compose-handoff.md`
- `design-system/pickit/MASTER.md`
- `design-system/pickit/pages/*.md`

## 2026-03-29

### 工程初始化

- 从零搭建 Android 工程：Gradle、`app` 模块、Compose、Hilt、Room、Retrofit、WorkManager
- 建立 `data / domain / presentation / di` 分层骨架
- 落地首页、新增、识别预览、详情、统计、设置六个主页面
- 生成 `gradlew` / `gradlew.bat`，完成可本地构建的基础工程

### 构建验证

- 使用本机 Android SDK 34
- 通过 `assembleDebug`
- APK 产物位于 `app/build/outputs/apk/debug/app-debug.apk`

### 本地数据链路落地

- `ProductRepository` 切换为 Room 实现，支持商品、标签关联和价格历史落库
- 识别预览保存后会真实写入本地数据库，不再停留在内存仓库
- `Settings` 接入 DataStore，持久化 `apiBaseUrl` 与 `webDavPath`
- 修正 Room 索引与若干低成本 warning

### 识别链路收口

- 新增图片选择与相机拍摄入口，支持从新增页进入识别预览
- 预览页改为读取导航传入的图片 URI 与备注
- `RemoteParseRepository` 接入真实 multipart 上传与动态接口地址
- 增加解析日志写入 `parse_logs`，记录成功/失败与错误信息
- 修复此前多处中文乱码，恢复主流程界面可读性

### 同步与备份收口

- 移除 `MockSyncRepository`，新增 `RoomSyncRepository`
- 支持导出 JSON 到本地文件
- 支持从本地 JSON 导入并覆盖当前数据库
- 支持通过 WebDAV 地址执行真实备份与恢复
- 设置页补齐本地导入导出、WebDAV 备份恢复与风险提示

### 当前状态

- 设计文档、Android 工程、主页面骨架、识别链路、本地持久化、备份恢复均已打通
- 当前默认约定的 WebDAV 地址拼接规则为：`API 地址 + WebDAV 路径 + pickit-backup.json`
- 若后续需要接入鉴权、增量同步、多设备冲突合并，可在此基础上继续扩展
