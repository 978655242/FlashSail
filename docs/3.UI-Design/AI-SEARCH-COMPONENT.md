# FlashSell AI 闪选菜单设计

## 设计理念

**"从搜索框到AI对话"** - 将传统搜索转变为智能对话体验

---

## 核心特性

### 1. 对话式搜索输入
- 自然语言理解
- 语音输入支持
- 实时智能建议
- 流式响应动画

### 2. 上下文智能建议
- 基于用户意图的动态建议
- 历史搜索快速访问
- 热门选品场景
- AI推荐的优化查询

### 3. 渐进式引导
- 新手引导提示
- 快速操作芯片
- 示例查询展示
- 交互式教程

---

## 设计规范

### 容器布局

```
┌─────────────────────────────────────────────────────────────┐
│                    🤖 FlashSell AI                           │
│                  ┌─────────────────────┐                    │
│                  │  💬 我在找...       │                    │
│                  └─────────────────────┘                    │
│  [利润高的产品] [蓝海市场] [趋势爆款] [低竞争]               │
└─────────────────────────────────────────────────────────────┘
```

### 组件层级

```css
/* 1. 主容器 */
.ai-search-hero {
    position: relative;
    max-width: 900px;
    margin: 0 auto;
    padding: 48px 32px;
}

/* 2. AI助手头部 */
.ai-assistant-header {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-bottom: 32px;
    animation: fadeInDown 0.6s ease-out;
}

.ai-avatar {
    width: 80px;
    height: 80px;
    background: linear-gradient(135deg, #F97316 0%, #FB923C 100%);
    border-radius: 24px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 16px;
    box-shadow: 0 8px 32px rgba(249, 115, 22, 0.3);
    animation: pulse-glow 2s ease-in-out infinite;
}

@keyframes pulse-glow {
    0%, 100% {
        box-shadow: 0 8px 32px rgba(249, 115, 22, 0.3);
    }
    50% {
        box-shadow: 0 8px 48px rgba(249, 115, 22, 0.5);
    }
}

.ai-greeting {
    font-size: 28px;
    font-weight: 700;
    color: #F8FAFC;
    text-align: center;
    margin-bottom: 8px;
}

.ai-subtitle {
    font-size: 16px;
    color: #94A3B8;
    text-align: center;
}
```

### AI搜索框

```css
/* 3. 搜索输入容器 */
.ai-search-container {
    position: relative;
    margin-bottom: 16px;
}

.ai-search-box {
    display: flex;
    align-items: center;
    gap: 12px;
    background: rgba(30, 41, 59, 0.8);
    backdrop-filter: blur(20px);
    border: 2px solid rgba(51, 65, 85, 0.5);
    border-radius: 20px;
    padding: 8px 8px 8px 24px;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.ai-search-box:focus-within {
    border-color: rgba(249, 115, 22, 0.6);
    box-shadow:
        0 0 0 4px rgba(249, 115, 22, 0.1),
        0 8px 32px rgba(249, 115, 22, 0.2);
    transform: translateY(-2px);
}

.ai-search-icon {
    width: 24px;
    height: 24px;
    color: #64748B;
    transition: color 0.3s ease;
}

.ai-search-box:focus-within .ai-search-icon {
    color: #F97316;
}

.ai-search-input {
    flex: 1;
    height: 48px;
    font-size: 17px;
    font-weight: 400;
    color: #F8FAFC;
    background: transparent;
    border: none;
    outline: none;
}

.ai-search-input::placeholder {
    color: #64748B;
    transition: color 0.3s ease;
}

.ai-search-box:focus-within .ai-search-input::placeholder {
    color: #94A3B8;
}

/* 语音输入按钮 */
.voice-input-btn {
    width: 48px;
    height: 48px;
    background: rgba(59, 130, 246, 0.1);
    border: 1px solid rgba(59, 130, 246, 0.2);
    border-radius: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.3s ease;
}

.voice-input-btn:hover {
    background: rgba(59, 130, 246, 0.15);
    border-color: rgba(59, 130, 246, 0.4);
    transform: scale(1.05);
}

.voice-input-btn.recording {
    background: rgba(239, 68, 68, 0.15);
    border-color: rgba(239, 68, 68, 0.4);
    animation: recording-pulse 1.5s ease-in-out infinite;
}

@keyframes recording-pulse {
    0%, 100% {
        box-shadow: 0 0 0 0 rgba(239, 68, 68, 0.4);
    }
    50% {
        box-shadow: 0 0 0 12px rgba(239, 68, 68, 0);
    }
}

/* 提交按钮 */
.ai-submit-btn {
    width: 48px;
    height: 48px;
    background: linear-gradient(135deg, #F97316 0%, #EA580C 100%);
    border: none;
    border-radius: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.3s ease;
}

.ai-submit-btn:hover {
    transform: scale(1.05);
    box-shadow: 0 8px 24px rgba(249, 115, 22, 0.4);
}

.ai-submit-btn:active {
    transform: scale(0.98);
}

.ai-submit-btn:disabled {
    background: rgba(51, 65, 85, 0.5);
    cursor: not-allowed;
    transform: none;
}
```

### 智能建议下拉框

```css
/* 4. 智能建议下拉框 */
.ai-suggestions-dropdown {
    position: absolute;
    top: calc(100% + 8px);
    left: 0;
    right: 0;
    background: rgba(30, 41, 59, 0.95);
    backdrop-filter: blur(20px);
    border: 1px solid rgba(51, 65, 85, 0.6);
    border-radius: 16px;
    overflow: hidden;
    opacity: 0;
    visibility: hidden;
    transform: translateY(-10px);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    z-index: 50;
}

.ai-suggestions-dropdown.visible {
    opacity: 1;
    visibility: visible;
    transform: translateY(0);
}

.suggestions-header {
    padding: 16px 20px;
    background: rgba(249, 115, 22, 0.08);
    border-bottom: 1px solid rgba(51, 65, 85, 0.5);
}

.suggestions-header-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 13px;
    font-weight: 600;
    color: #F97316;
    text-transform: uppercase;
    letter-spacing: 0.5px;
}

.suggestions-list {
    max-height: 300px;
    overflow-y: auto;
}

.suggestions-list::-webkit-scrollbar {
    width: 6px;
}

.suggestions-list::-webkit-scrollbar-track {
    background: rgba(51, 65, 85, 0.3);
}

.suggestions-list::-webkit-scrollbar-thumb {
    background: rgba(51, 65, 85, 0.6);
    border-radius: 3px;
}

.suggestion-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 14px 20px;
    cursor: pointer;
    transition: all 0.2s ease;
    border-bottom: 1px solid rgba(51, 65, 85, 0.3);
}

.suggestion-item:last-child {
    border-bottom: none;
}

.suggestion-item:hover {
    background: rgba(249, 115, 22, 0.1);
}

.suggestion-item.selected {
    background: rgba(249, 115, 22, 0.15);
    border-left: 3px solid #F97316;
    padding-left: 17px;
}

.suggestion-icon {
    width: 20px;
    height: 20px;
    color: #64748B;
    flex-shrink: 0;
}

.suggestion-content {
    flex: 1;
    min-width: 0;
}

.suggestion-title {
    font-size: 15px;
    font-weight: 500;
    color: #F8FAFC;
    margin-bottom: 2px;
}

.suggestion-description {
    font-size: 13px;
    color: #64748B;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.suggestion-arrow {
    width: 16px;
    height: 16px;
    color: #64748B;
    flex-shrink: 0;
}
```

### 快速操作芯片

```css
/* 5. 快速操作芯片 */
.quick-actions-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
    margin-top: 24px;
}

.quick-actions-label {
    font-size: 13px;
    color: #64748B;
}

.quick-actions-chips {
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
    justify-content: center;
}

.action-chip {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    background: rgba(51, 65, 85, 0.4);
    border: 1px solid rgba(51, 65, 85, 0.6);
    border-radius: 100px;
    padding: 10px 18px;
    font-size: 14px;
    color: #94A3B8;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    user-select: none;
}

.action-chip:hover {
    background: rgba(249, 115, 22, 0.12);
    border-color: rgba(249, 115, 22, 0.4);
    color: #F97316;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(249, 115, 22, 0.2);
}

.action-chip:active {
    transform: translateY(0);
}

.action-chip-emoji {
    font-size: 16px;
}

/* 特殊芯片样式 */
.action-chip.hot {
    background: rgba(239, 68, 68, 0.1);
    border-color: rgba(239, 68, 68, 0.3);
}

.action-chip.hot:hover {
    background: rgba(239, 68, 68, 0.15);
    border-color: rgba(239, 68, 68, 0.5);
    color: #EF4444;
}

.action-chip.trend {
    background: rgba(16, 185, 129, 0.1);
    border-color: rgba(16, 185, 129, 0.3);
}

.action-chip.trend:hover {
    background: rgba(16, 185, 129, 0.15);
    border-color: rgba(16, 185, 129, 0.5);
    color: #10B981;
}
```

### AI思考状态

```css
/* 6. AI思考状态 */
.ai-thinking-container {
    display: none;
    align-items: center;
    gap: 16px;
    padding: 20px 24px;
    background: rgba(30, 41, 59, 0.6);
    border: 1px solid rgba(51, 65, 85, 0.5);
    border-radius: 16px;
    margin-top: 24px;
    animation: fadeIn 0.3s ease;
}

.ai-thinking-container.active {
    display: flex;
}

.ai-thinking-spinner {
    width: 32px;
    height: 32px;
    position: relative;
}

.ai-thinking-spinner svg {
    animation: rotate 2s linear infinite;
}

@keyframes rotate {
    from {
        transform: rotate(0deg);
    }
    to {
        transform: rotate(360deg);
    }
}

.ai-thinking-content {
    flex: 1;
}

.ai-thinking-text {
    font-size: 15px;
    font-weight: 500;
    color: #F8FAFC;
    margin-bottom: 8px;
}

.ai-thinking-steps {
    display: flex;
    flex-direction: column;
    gap: 6px;
}

.thinking-step {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 13px;
    color: #64748B;
}

.thinking-step.completed {
    color: #10B981;
}

.thinking-step.active {
    color: #F97316;
}

.thinking-step-icon {
    width: 14px;
    height: 14px;
    border-radius: 50%;
    border: 2px solid currentColor;
    display: flex;
    align-items: center;
    justify-content: center;
}

.thinking-step.completed .thinking-step-icon {
    background: currentColor;
}

.thinking-step.active .thinking-step-icon {
    border-style: dotted;
    animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
    0%, 100% {
        opacity: 1;
    }
    50% {
        opacity: 0.5;
    }
}

/* 进度条 */
.ai-progress-bar {
    height: 3px;
    background: rgba(51, 65, 85, 0.5);
    border-radius: 2px;
    overflow: hidden;
    margin-top: 12px;
}

.ai-progress-fill {
    height: 100%;
    background: linear-gradient(90deg, #F97316 0%, #FB923C 100%);
    transition: width 0.3s ease;
}
```

### 历史搜索

```css
/* 7. 历史搜索 */
.recent-searches-container {
    margin-top: 32px;
    animation: fadeInUp 0.6s ease 0.2s both;
}

.recent-searches-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 16px;
}

.recent-searches-title {
    font-size: 14px;
    font-weight: 600;
    color: #64748B;
}

.recent-searches-clear {
    font-size: 13px;
    color: #94A3B8;
    cursor: pointer;
    transition: color 0.2s ease;
}

.recent-searches-clear:hover {
    color: #F8FAFC;
}

.recent-searches-list {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
}

.recent-search-tag {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    background: rgba(51, 65, 85, 0.3);
    border-radius: 8px;
    padding: 8px 14px;
    font-size: 14px;
    color: #94A3B8;
    cursor: pointer;
    transition: all 0.2s ease;
}

.recent-search-tag:hover {
    background: rgba(51, 65, 85, 0.5);
    color: #F8FAFC;
}

.recent-search-tag-icon {
    width: 14px;
    height: 14px;
    color: #64748B;
}
```

---

## 响应式设计

```css
/* 移动端适配 */
@media (max-width: 768px) {
    .ai-search-hero {
        padding: 32px 20px;
    }

    .ai-avatar {
        width: 64px;
        height: 64px;
        border-radius: 20px;
    }

    .ai-greeting {
        font-size: 22px;
    }

    .ai-subtitle {
        font-size: 14px;
    }

    .ai-search-box {
        padding: 6px 6px 6px 16px;
    }

    .ai-search-input {
        height: 44px;
        font-size: 16px;
    }

    .voice-input-btn,
    .ai-submit-btn {
        width: 44px;
        height: 44px;
    }

    .quick-actions-chips {
        gap: 8px;
    }

    .action-chip {
        padding: 8px 14px;
        font-size: 13px;
    }

    .suggestion-item {
        padding: 12px 16px;
    }
}
```

---

## 浅色模式适配

```css
/* 浅色模式 */
html.light .ai-search-box {
    background: rgba(255, 255, 255, 0.9);
    border-color: rgba(203, 213, 225, 0.8);
}

html.light .ai-search-box:focus-within {
    border-color: rgba(249, 115, 22, 0.4);
    box-shadow:
        0 0 0 4px rgba(249, 115, 22, 0.08),
        0 8px 24px rgba(249, 115, 22, 0.12);
}

html.light .ai-search-input {
    color: #0F172A;
}

html.light .ai-search-input::placeholder {
    color: #94A3B8;
}

html.light .ai-suggestions-dropdown {
    background: rgba(255, 255, 255, 0.98);
    border-color: rgba(203, 213, 225, 0.8);
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

html.light .suggestions-header {
    background: rgba(249, 115, 22, 0.06);
    border-bottom-color: rgba(203, 213, 225, 0.6);
}

html.light .suggestion-item {
    border-bottom-color: rgba(203, 213, 225, 0.5);
}

html.light .suggestion-item:hover {
    background: rgba(249, 115, 22, 0.06);
}

html.light .action-chip {
    background: rgba(241, 245, 249, 0.8);
    border-color: rgba(203, 213, 225, 0.8);
    color: #475569;
}

html.light .action-chip:hover {
    background: rgba(249, 115, 22, 0.08);
    border-color: rgba(249, 115, 22, 0.3);
}

html.light .recent-search-tag {
    background: rgba(241, 245, 249, 0.8);
    color: #475569;
}

html.light .recent-search-tag:hover {
    background: rgba(226, 232, 240, 0.8);
}

html.light .ai-thinking-container {
    background: rgba(255, 255, 255, 0.8);
    border-color: rgba(203, 213, 225, 0.6);
}

html.light .ai-greeting {
    color: #0F172A;
}

html.light .ai-subtitle {
    color: #64748B;
}
```

---

## 交互状态

### 输入状态

```css
/* 空状态 */
.ai-search-box.empty .ai-submit-btn {
    opacity: 0.5;
    pointer-events: none;
}

/* 输入中 */
.ai-search-box.typing .ai-submit-btn {
    opacity: 1;
    pointer-events: auto;
}

/* 加载中 */
.ai-search-box.loading .ai-submit-btn {
    pointer-events: none;
}

.ai-search-box.loading .ai-submit-btn svg {
    animation: rotate 1s linear infinite;
}
```

### 键盘导航

```css
/* 键盘焦点可见 */
.ai-search-box:focus-within {
    outline: none;
}

.suggestion-item:focus {
    outline: none;
    background: rgba(249, 115, 22, 0.15);
    border-left-color: #F97316;
}

.action-chip:focus {
    outline: none;
    border-color: #F97316;
    box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.2);
}
```

---

## 动画效果

### 入场动画

```css
@keyframes fadeInDown {
    from {
        opacity: 0;
        transform: translateY(-20px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

@keyframes fadeInUp {
    from {
        opacity: 0;
        transform: translateY(20px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

@keyframes fadeIn {
    from {
        opacity: 0;
    }
    to {
        opacity: 1;
    }
}

/* 交错动画 */
.stagger-1 { animation-delay: 0.1s; }
.stagger-2 { animation-delay: 0.2s; }
.stagger-3 { animation-delay: 0.3s; }
.stagger-4 { animation-delay: 0.4s; }
```

---

## 无障碍支持

```css
/* 减少动画 */
@media (prefers-reduced-motion: reduce) {
    *,
    *::before,
    *::after {
        animation-duration: 0.01ms !important;
        animation-iteration-count: 1 !important;
        transition-duration: 0.01ms !important;
    }
}

/* 高对比度 */
@media (prefers-contrast: high) {
    .ai-search-box {
        border-width: 2px;
    }

    .action-chip {
        border-width: 2px;
    }
}
```

---

*设计版本: v3.0 - AI交互式闪选菜单*
*更新日期: 2025-01-14*
