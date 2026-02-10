# FlashSell UI 设计规范

## 设计概述

FlashSell 是一款面向跨境电商卖家的AI驱动爆品选品工具。UI设计强调**科技感**与**易用性**，采用深色主题+玻璃拟态+橙色点缀的设计风格。

---

## 设计原则

1. **科技感** - 深色背景、渐变光效、流畅动画
2. **易用性** - 清晰的层级结构、直观的操作流程
3. **数据驱动** - 数据可视化优先，重要信息突出显示
4. **一致性** - 统一的组件、颜色、间距规范

---

## 配色方案

### 主色调

| 用途 | 色值 | 说明 |
|-----|------|------|
| 主色（橙色） | `#F97316` | CTA按钮、重要操作、品牌色 |
| 主色悬停 | `#EA580C` | 按钮悬停状态 |
| 次色（蓝色） | `#3B82F6` | 数据可视化、成功状态 |

### 背景色

| 用途 | 色值 | 说明 |
|-----|------|------|
| 深色背景 | `#0F172A` | 主背景色 |
| 卡片背景 | `#1E293B` | 卡片、面板背景 |
| 卡片悬停 | `#334155` | 卡片悬停状态 |
| 边框 | `#334155` | 分隔线、边框 |

### 文字色

| 用途 | 色值 | 说明 |
|-----|------|------|
| 主要文字 | `#F8FAFC` | 标题、重要内容 |
| 次要文字 | `#94A3B8` | 描述、辅助信息 |
| 弱化文字 | `#64748B` | 占位符、禁用状态 |

### 语义色

| 用途 | 色值 | 说明 |
|-----|------|------|
| 成功 | `#10B981` | 增长、正向数据 |
| 警告 | `#F59E0B` | 需注意、中等风险 |
| 危险 | `#EF4444` | 下降、风险预警、高竞争 |

---

## 字体规范

### 字体族

| 用途 | 字体 | 字重 |
|-----|------|------|
| 中文字体 | Noto Sans SC | 300/400/500/600/700 |
| 英文字体 | Inter | 300/400/500/600/700 |
| 数字/代码 | Inter | 400/500/600 |

### 字号规范

| 用途 | 字号 | 行高 |
|-----|------|------|
| 大标题 | 32px (2rem) | 1.2 |
| 标题 | 24px (1.5rem) | 1.3 |
| 小标题 | 20px (1.25rem) | 1.4 |
| 正文 | 16px (1rem) | 1.5 |
| 辅助文字 | 14px (0.875rem) | 1.5 |
| 小字 | 12px (0.75rem) | 1.4 |

---

## 间距系统

使用 4px 基础单位的间距系统：

- `xs`: 4px
- `sm`: 8px
- `md`: 16px
- `lg`: 24px
- `xl`: 32px
- `2xl`: 48px

---

## 圆角规范

| 用途 | 圆角值 |
|-----|-------|
| 小按钮 | 8px |
| 中按钮/输入框 | 10px-12px |
| 卡片 | 16px |
| 大卡片/弹窗 | 20px |
| 标签 | 20px (全圆角) |

---

## 阴影与光效

### 卡片阴影

```css
.glass-card {
    background: rgba(30, 41, 59, 0.7);
    backdrop-filter: blur(12px);
    border: 1px solid rgba(51, 65, 85, 0.5);
    border-radius: 16px;
}
```

### 按钮光效

```css
.btn-primary {
    box-shadow: 0 4px 14px rgba(249, 115, 22, 0.25);
}

.btn-primary:hover {
    box-shadow: 0 8px 24px rgba(249, 115, 22, 0.35);
}
```

---

## 组件规范

### 按钮

```html
<!-- 主按钮 -->
<button class="btn-primary">
    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <!-- 图标 -->
    </svg>
    按钮文字
</button>

<!-- 次要按钮 -->
<button class="btn-secondary">按钮文字</button>

<!-- 幽灵按钮 -->
<button class="btn-ghost">按钮文字</button>
```

### 输入框

```html
<input
    type="text"
    class="search-input"
    placeholder="提示文字"
>
```

### 卡片

```html
<div class="glass-card p-6">
    <!-- 内容 -->
</div>
```

### 标签

```html
<span class="badge badge-hot">热门</span>
<span class="badge badge-trend">趋势</span>
<span class="badge badge-warning">警告</span>
```

---

## 页面布局

### 侧边栏导航

- 宽度: 260px
- 固定定位左侧
- 圆角: 16px
- 背景: rgba(15, 23, 42, 0.9)

### 主内容区

- 左 margin: 260px
- 内边距: 24px 32px
- 最大宽度: 无限制

### 响应式断点

| 断点 | 宽度 | 行为 |
|-----|------|------|
| sm | 640px | - |
| md | 768px | - |
| lg | 1024px | 侧边栏折叠 |
| xl | 1280px | - |
| 2xl | 1536px | - |

---

## 图标规范

使用 Heroicons 或 Lucide Icons，尺寸规范：

| 用途 | 尺寸 |
|-----|------|
| 小图标 | 16px (w-4 h-4) |
| 常规图标 | 20px (w-5 h-5) |
| 大图标 | 24px (w-6 h-6) |
| 超大图标 | 32px (w-8 h-8) |

---

## 动画规范

### 过渡时长

| 类型 | 时长 |
|-----|------|
| 快速 | 150ms |
| 常规 | 200ms-300ms |
| 慢速 | 400ms-500ms |

### 常用动画

```css
/* 悬停上移 */
.product-card:hover {
    transform: translateY(-4px);
}

/* 淡入动画 */
@keyframes fadeIn {
    from { opacity: 0; transform: translateY(10px); }
    to { opacity: 1; transform: translateY(0); }
}

.animate-fade-in {
    animation: fadeIn 0.3s ease forwards;
}
```

---

## 数据可视化

### 图表配色

| 图表类型 | 推荐库 | 主色调 |
|---------|-------|-------|
| 趋势图 | Chart.js / ECharts | #F97316 (橙) |
| 柱状图 | Chart.js | #3B82F6 (蓝) |
| 饼图 | Chart.js | 多色渐变 |
| 折线图 | Chart.js | #10B981 (绿) |

### 关键数据样式

```css
.stat-value {
    font-size: 2rem;
    font-weight: 700;
    color: #F8FAFC;
}

.stat-change.positive {
    background: rgba(16, 185, 129, 0.15);
    color: #10B981;
}

.stat-change.negative {
    background: rgba(239, 68, 68, 0.15);
    color: #EF4444;
}
```

---

## 设计资源

### Google Fonts

```css
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&family=Noto+Sans+SC:wght@300;400;500;600;700&display=swap');
```

### Tailwind CSS 配置

```javascript
// tailwind.config.js
module.exports = {
    theme: {
        extend: {
            fontFamily: {
                sans: ['Inter', 'Noto Sans SC', 'sans-serif'],
            },
            colors: {
                primary: {
                    DEFAULT: '#F97316',
                    hover: '#EA580C',
                },
                secondary: '#3B82F6',
            },
        },
    },
}
```

---

## 页面清单

| 页面 | 优先级 | 描述 |
|-----|-------|------|
| 仪表盘 | P0 | 核心数据概览、AI推荐 |
| AI选品搜索 | P0 | 关键词搜索、高级筛选 |
| 产品详情 | P0 | 产品信息、价格趋势 |
| 我的收藏 | P0 | 收藏管理、看板 |
| 市场分析 | P1 | 品类趋势、竞争分析 |
| 订阅套餐 | P0 | 定价、升级 |
| 个人中心 | P1 | 账户设置、使用统计 |
| 登录/注册 | P0 | 认证流程 |

---

## 设计检查清单

在交付设计前确认：

- [ ] 颜色对比度符合 WCAG AA 标准
- [ ] 交互元素有悬停/焦点状态
- [ ] 动画时长在 150-300ms 范围
- [ ] 深色模式下文字清晰可读
- [ ] 移动端响应式布局正常
- [ ] 重要操作有明确的视觉反馈

---

*文档版本: v1.0*
*更新日期: 2025-01-13*
