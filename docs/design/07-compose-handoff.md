# PickIt Compose 设计交接

## 1. 总体实现原则

- 使用 Material 3 作为基础，但不要直接套默认视觉
- 将设计令牌映射为自定义 `ColorScheme`、`Typography`、`Shapes`
- 导航使用事件驱动，不在 UI 层直接持有业务跳转逻辑
- 所有关键点击区域提供 `contentDescription` 与必要的 `semantics`

## 2. 主题映射

### Colors

- `primary` -> `brandPrimary`
- `primaryContainer` -> `brandPrimaryContainer`
- `secondary` -> `brandSecondary`
- `tertiary` -> `actionAccent`
- `background` -> `surfaceBase`
- `surface` -> `surfaceCard`
- `error` -> `error`

### Typography

- `displaySmall` -> 首页空态与统计大数字
- `headlineMedium` -> 页面标题
- `titleLarge` -> 模块标题
- `bodyLarge` -> 表单正文和卡片正文
- `labelLarge` -> 按钮与状态胶囊

### Shapes

- `small` -> 10dp
- `medium` -> 16dp
- `large` -> 24dp

## 3. 组件建议

### 首页

- 搜索栏：`SearchBar` 风格自定义实现
- 筛选条：横向 `LazyRow`
- 商品列表：`LazyColumn`
- 新增按钮：扩展型 `FloatingActionButton`

### 新增页

- 图片选择区：自定义 `Card`
- 备注输入：多行 `OutlinedTextField`
- 底部按钮：固定底栏 + `Button`

### 识别预览页

- 表单：分组 `OutlinedTextField` + 下拉选择
- 标签编辑：`FlowRow` + 可删除 Chip
- 置信提示：`AssistChip` 或轻量 Banner

### 详情页

- 信息分组：多个卡片分段
- 状态切换：`ModalBottomSheet`
- 价格历史：时间倒序 `LazyColumn`

### 统计页

- 先用纯 Compose 基础图形或简单条形块
- 不引入重型图表库作为 P0 前置条件

## 4. 可访问性约束

- 所有图标按钮必须带 `contentDescription`
- 标签和状态不能只靠颜色区分
- 表单错误提示放在字段下方，不只改边框颜色
- 固定底部按钮要兼容小屏和输入法抬升

## 5. P0 实现优先级

1. `PickItTheme` 与设计令牌落地
2. 首页骨架与商品卡片
3. 新增页与识别中状态
4. 识别预览表单
5. 详情页信息卡与价格历史
6. 统计页轻量版
7. 设置页与备份入口

## 6. 推荐组件目录

```text
presentation/
  component/
    PickItTopBar.kt
    PickItSearchBar.kt
    FilterChipRow.kt
    ProductCard.kt
    StatusChip.kt
    TagChip.kt
    SectionCard.kt
    EmptyState.kt
    LoadingState.kt
    ErrorState.kt
  theme/
    Color.kt
    Theme.kt
    Type.kt
    Shape.kt
```

## 7. 设计验收标准

- 首页首屏可在 375dp 宽度下完整显示搜索、筛选、至少 2 张商品卡片
- 识别预览页单手操作下，底部保存按钮始终可见
- 详情页阅读顺序符合“标题 -> 价格 -> 摘要 -> 标签 -> 历史”
- 所有空态、错误态、加载态都有明确文案和可执行动作
