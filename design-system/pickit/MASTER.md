# PickIt Design System Master

> 构建具体页面前，优先检查 `design-system/pickit/pages/[page-name].md`。
> 若页面规则存在，以页面规则覆盖本文件；否则以本文件为全局基线。

---

**Project:** PickIt / 拾物
**Updated:** 2026-03-28
**Category:** Android mobile utility app

---

## Product Intent

PickIt 是一个 AI 商品灵感收藏与待购管理工具。

设计目标：

- 帮用户把“先截图保存”变成“可管理、可检索、可追踪”
- 优先支持图片录入、AI 预填、快速修正、后续回看
- 保持工具型 App 的可信秩序感，不做促销页视觉

## Global Rules

### Style Direction

- Base style: Calm utility + editorial order
- Structural style: Light bento modularity
- Motion style: Micro-interactions only
- Avoid: ecommerce promo noise, hero banners, heavy gradients, glassmorphism overload

### Color Palette

| Role | Hex | Token |
|---|---|---|
| Primary | `#0D9488` | `brandPrimary` |
| Primary Container | `#CCFBF1` | `brandPrimaryContainer` |
| Secondary | `#14B8A6` | `brandSecondary` |
| Action Accent | `#F97316` | `actionAccent` |
| Background | `#F8FAFC` | `surfaceBase` |
| Surface | `#FFFFFF` | `surfaceCard` |
| Surface Muted | `#ECFDF5` | `surfaceMuted` |
| Text Primary | `#0F172A` | `textPrimary` |
| Text Secondary | `#475569` | `textSecondary` |
| Border | `#DCE7EF` | `borderSubtle` |
| Warning | `#F59E0B` | `warning` |
| Error | `#DC2626` | `error` |
| Success | `#16A34A` | `success` |

### Typography

- Primary font: `Noto Sans SC` or Android system Chinese sans
- Numeric / English support font: `Rubik`
- Tone: clean, readable, modern, not overly geometric

### Spacing

| Token | Value |
|---|---|
| `spaceXs` | `4dp` |
| `spaceSm` | `8dp` |
| `spaceMd` | `12dp` |
| `spaceLg` | `16dp` |
| `spaceXl` | `24dp` |
| `space2xl` | `32dp` |

### Radius

| Token | Value |
|---|---|
| `radiusSmall` | `10dp` |
| `radiusMedium` | `16dp` |
| `radiusLarge` | `24dp` |
| `radiusFull` | `999dp` |

### Elevation

- Use very light card elevation
- Prefer border + tonal surface over strong shadows

## Component Rules

### Buttons

- Primary buttons use `actionAccent`
- High-frequency actions use rounded full pill or 16dp radius
- Destructive actions use `error` only when action is irreversible

### Search

- Search field must be large enough for one-handed mobile use
- Placeholder text should explain searchable scope
- Clear action visible while typing

### Cards

- Product cards are white surfaces on a soft cool background
- Strongest contrast is reserved for title, price, and active status
- No image-dependent layout assumptions

### Chips

- Use chips for status, platform, and tags
- Never rely only on color to distinguish state

### Forms

- AI-filled fields remain editable
- Low-confidence fields get subtle warning highlight
- Field grouping should reduce perceived form length

## Motion

- Press feedback: `80ms`
- Filter change: `160ms`
- Card entry: `220ms`
- Bottom sheet: `240ms`
- Respect reduced-motion settings

## Accessibility Rules

- All icon-only actions require `contentDescription`
- Form errors must have text, not just red border
- Status must include text label, not only color
- Fixed bottom CTA must remain visible above IME

## Anti-Patterns

- Do not use livestream commerce reds and yellows as base UI
- Do not create oversized promotional banners on Home
- Do not hide important metadata behind secondary menus
- Do not make navigation depend on replayable state streams

## Delivery Checklist

- [ ] Home first screen is usable at 375dp width
- [ ] Search, filter, add flow reachable with one hand
- [ ] Preview page clearly marks low-confidence fields
- [ ] Detail page follows title -> price -> summary -> tags -> history order
- [ ] Backup and restore flows include explicit risk wording
