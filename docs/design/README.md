# PickIt App 设计总览

本目录用于沉淀 PickIt（拾物）Android 客户端的设计全过程交付，供后续产品、UI、Compose 开发直接对齐执行。

## 交付清单

- `00-worklog.md`：设计工作日志与决策记录
- `01-design-strategy.md`：产品定位、用户心智、设计原则、视觉方向
- `02-information-architecture.md`：信息架构、导航结构、页面分层
- `03-user-flows.md`：关键任务流、异常流、状态流转
- `04-screen-specs.md`：核心页面结构、模块说明、交互重点
- `05-visual-system.md`：颜色、字体、栅格、圆角、图标、动效、组件基线
- `06-copy-and-states.md`：界面文案、空态、错误态、反馈文案规范
- `07-compose-handoff.md`：Jetpack Compose 落地约束、组件映射、实现优先级

## 当前设计结论

- 产品类型：AI 商品灵感收藏与待购管理工具
- 主要平台：Android 手机，输入以图片为主
- 体验关键词：轻量、清晰、可信、可回看、有温度
- 视觉方向：工具型秩序感 + 商品灵感感，不做促销页视觉噪音
- 技术落地：Jetpack Compose + Material 3 自定义主题

## 设计执行顺序

1. 先完成 P0 结构设计：首页、新增页、识别预览页、详情页、统计页、设置页
2. 再补充 P1 筛选增强、CSV 导出、标签统计、自动备份相关交互
3. 最后扩展 P2 的批量导入、分享接收、价格趋势图
