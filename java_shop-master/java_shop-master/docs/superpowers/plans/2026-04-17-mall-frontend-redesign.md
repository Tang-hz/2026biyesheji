# 商城用户页面企业级重构实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 对6个核心用户页面进行企业级精致化重构，保持蓝白色主色调，参考无印良品/小米有品的简洁克制风格

**Architecture:** 在现有 Vue 3 + Ant Design Vue + Less 技术栈上，通过增强设计系统 token、优化组件样式、添加微妙交互动效，实现企业级视觉升级

**Tech Stack:** Vue 3, Less, Ant Design Vue, 现有 base.less 设计系统

---

## 文件修改概览

| 页面 | 修改文件 | 主要改动 |
|------|---------|---------|
| 首页 | `portal.vue` | 广告弹窗 → 侧边 slide-in 卡片 |
| 首页内容 | `content.vue` | 产品卡片 hover 增强、KingKong 优化、Banner dots 样式 |
| Header | `header.vue` | 搜索框 focus 动画、购物车 badge 动画 |
| 商品详情 | `detail.vue` | 图片悬停放大、购买按钮增强、Tab 下划线动画 |
| 搜索页 | `search.vue` + `search-content-view.vue` | 左侧筛选面板、排序 tab 动画、结果卡片精致化 |
| 购物车 | `cart.vue` | 商品行 hover 效果、价格区域卡片化 |
| 确认订单 | `confirm.vue` | 地址卡片边框、结算区域分隔线 |
| 支付页 | `pay.vue` | 成功信息样式、按钮一致性 |
| 设计系统 | `base.less` | 微调部分 token（如需要） |

---

## 实现任务

### Task 1: 首页广告弹窗改为侧边 slide-in 卡片

**Files:**
- Modify: `web/src/views/index/portal.vue`

**Changes:**

- [ ] **Step 1: 修改 portal.vue — 将 `a-modal` 替换为固定定位 slide-in 卡片**

替换模态框逻辑为右侧滑入的通知卡片：

```vue
<!-- 删除 a-modal，替换为 -->
<div v-if="adModal.visible" class="slide-in-notification" @click="handleAdClick(adModal.item)">
  <div class="notification-close" @click.stop="handleCloseAd">×</div>
  <img v-if="adModal.item" :src="adModal.item.imageUrl" class="notification-img" alt="广告" />
  <div v-if="adModal.item" class="notification-slogan">{{ adModal.item.slogan }}</div>
</div>
```

新增 Less 样式：

```less
.slide-in-notification {
  position: fixed;
  right: 24px;
  top: 100px;
  width: 280px;
  background: @white;
  border-radius: @radius-xl;
  box-shadow: @shadow-xl;
  border: 1px solid @border-light;
  padding: 16px;
  cursor: pointer;
  z-index: @z-popover;
  animation: slideInRight 400ms cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
  transition: transform @transition-base, box-shadow @transition-base;

  &:hover {
    transform: translateX(-4px);
    box-shadow: @shadow-xl;
  }
}

.notification-close {
  position: absolute;
  top: 8px;
  right: 12px;
  font-size: 20px;
  color: @text-hint;
  cursor: pointer;
  transition: color @transition-fast;

  &:hover {
    color: @text-secondary;
  }
}

.notification-img {
  width: 100%;
  border-radius: @radius-lg;
  margin-bottom: 12px;
}

.notification-slogan {
  font-size: @font-size-sm;
  color: @text-secondary;
  text-align: center;
}

@keyframes slideInRight {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}
```

- [ ] **Step 2: 调整 `handleCloseAd` 方法**（已存在于原文件，确保关闭后隐藏 slide-in 卡片）

---

### Task 2: Header 交互优化

**Files:**
- Modify: `web/src/views/index/components/header.vue`

**Changes:**

- [ ] **Step 1: 修改 header.vue — 搜索框 focus 动画**

在 `.search-entry` 样式块中增强 focus 效果：

```less
.search-entry {
  // 现有样式保持，追加以下内容
  transition: box-shadow @transition-base, border-color @transition-base;

  &:focus-within {
    box-shadow: 0 0 0 3px rgba(70, 132, 226, 0.15);
    border-color: @primary-blue;
  }
}
```

- [ ] **Step 2: 购物车图标 badge 增加 scale 动画**

在 `.cart-bar-badge` 样式中追加 animation：

```less
.cart-bar-badge {
  // 现有样式保持
  animation: badgePop 300ms @transition-spring;
}

@keyframes badgePop {
  0% { transform: scale(0.5); }
  70% { transform: scale(1.1); }
  100% { transform: scale(1); }
}
```

---

### Task 3: 首页 Banner 轮播 dots 样式精致化

**Files:**
- Modify: `web/src/views/index/components/content.vue`

**Changes:**

- [ ] **Step 1: 修改 content.vue — Banner dots 圆角矩形样式**（已部分实现，确认并微调）

检查并确保以下样式存在：

```less
.mall-banner-wrap :deep(.slick-dots li button) {
  // 现有：background: rgba(255, 255, 255, 0.5);
  // 增强：
  background: rgba(255, 255, 255, 0.5);
  width: 6px;
  height: 6px;
  border-radius: 50%;
  transition: all @transition-fast;
}

.mall-banner-wrap :deep(.slick-dots li.slick-active button) {
  background: @white;
  width: 16px;
  border-radius: 3px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
}
```

- [ ] **Step 2: 增加 hover 时暂停轮播**（可选，在 carousel 标签上添加 `:pauseOnHover="true"`）

---

### Task 4: 产品卡片 hover 效果增强

**Files:**
- Modify: `web/src/views/index/components/content.vue`

**Changes:**

- [ ] **Step 1: 确认并增强 `.pc-thing-list .thing-item` hover 效果**

现有样式已包含基础 hover，确保以下完整：

```less
.pc-thing-list .thing-item {
  // 现有样式保持
  transition: transform @transition-base,
              box-shadow @transition-base,
              border-color @transition-base;

  &:hover {
    transform: translateY(-4px);
    box-shadow: @shadow-card-hover;
    border-color: @border-subtle;
  }

  // 新增：快速操作按钮 hover 时显示
  .quick-add-cart {
    opacity: 0.7;
    transition: opacity @transition-fast;
  }

  &:hover .quick-add-cart {
    opacity: 1;
  }
}
```

---

### Task 5: 商品详情页图片放大 + Tab 动画增强

**Files:**
- Modify: `web/src/views/index/detail.vue`

**Changes:**

- [ ] **Step 1: 修改 detail.vue — 主图悬停放大效果**

在 `.thing-img-box img` 样式中追加：

```less
.thing-img-box {
  overflow: hidden;
  border-radius: @radius-lg;

  img {
    transition: transform @transition-slow;
    cursor: zoom-in;
  }

  &:hover img {
    transform: scale(1.08);
  }
}
```

- [ ] **Step 2: 购买按钮 hover 阴影增强**

在 `.buy-btn` 样式中追加：

```less
.buy-btn {
  // 现有样式保持
  transition: all @transition-fast;

  &:hover {
    box-shadow: @shadow-button-hover;
    transform: translateY(-2px);
  }

  &:active {
    transform: translateY(0);
  }
}
```

- [ ] **Step 3: Tab 下划线动画流畅化**

在 `.tab-underline` 样式中确保 transition 正确：

```less
.tab-underline {
  transition: left @transition-base, width @transition-base;
}
```

---

### Task 6: 搜索页增强

**Files:**
- Modify: `web/src/views/index/search.vue`
- Modify: `web/src/views/index/components/search-content-view.vue`

**Changes:**

- [ ] **Step 1: 修改 search.vue — 增加左侧筛选面板布局**

在现有 1100px 容器内，增加左侧 200px 筛选区域：

```vue
<div class="search-layout">
  <aside class="search-sidebar">
    <div class="filter-panel">
      <h4>分类筛选</h4>
      <!-- 可扩展：分类树、标签等 -->
    </div>
  </aside>
  <div class="search-main">
    <SearchContentView />
  </div>
</div>
```

新增 Less 样式：

```less
.search-layout {
  display: flex;
  gap: 24px;
  max-width: 1100px;
  margin: 0 auto;
}

.search-sidebar {
  width: 180px;
  flex-shrink: 0;
}

.search-main {
  flex: 1;
  min-width: 0;
}

.filter-panel {
  background: @white;
  border-radius: @radius-lg;
  padding: 16px;
  box-shadow: @shadow-card;

  h4 {
    font-size: @font-size-lg;
    color: @navy-dark;
    margin: 0 0 12px;
    font-weight: 600;
  }
}
```

- [ ] **Step 2: 修改 search-content-view.vue — 结果卡片 hover 效果与首页一致**

确保 `.thing-item` 样式与 content.vue 中的产品卡片一致：

```less
.thing-item {
  background: @white;
  border-radius: @radius-lg;
  border: 1px solid @border-light;
  overflow: hidden;
  cursor: pointer;
  transition: all @transition-base;

  &:hover {
    transform: translateY(-4px);
    box-shadow: @shadow-card-hover;
    border-color: @border-subtle;
  }

  .img-view img {
    transition: transform @transition-slow;
  }

  &:hover .img-view img {
    transform: scale(1.05);
  }
}
```

---

### Task 7: 购物车精致化

**Files:**
- Modify: `web/src/views/index/cart.vue`

**Changes:**

- [ ] **Step 1: 购物车商品行 hover 效果**

在 `.items` 样式中追加：

```less
.items {
  // 现有样式保持
  transition: background @transition-fast, box-shadow @transition-base;

  &:hover {
    background: @bg-hover;
    box-shadow: @shadow-sm;
    border-radius: @radius-md;
  }
}
```

- [ ] **Step 2: 删除按钮 hover 变红**

在 `.delete` 样式中追加：

```less
.delete {
  cursor: pointer;
  transition: transform @transition-fast, opacity @transition-fast;

  &:hover {
    transform: scale(1.2);
    opacity: 0.8;
  }
}
```

- [ ] **Step 3: 价格汇总区域卡片化**

在 `.price-view` 样式中增强：

```less
.price-view {
  background: @bg-page;
  border-radius: @radius-lg;
  padding: 16px;
  border: 1px solid @border-light;
}
```

---

### Task 8: 确认订单页精致化

**Files:**
- Modify: `web/src/views/index/confirm.vue`

**Changes:**

- [ ] **Step 1: 地址卡片边框 + hover 效果**

在 `.address-view` 样式中增强：

```less
.address-view {
  background: @white;
  border-radius: @radius-lg;
  padding: 16px;
  border: 1px solid @border-light;
  transition: border-color @transition-fast, box-shadow @transition-fast;

  &:hover {
    border-color: @primary-blue;
    box-shadow: @shadow-sm;
  }
}
```

- [ ] **Step 2: 结算区域分隔线 + 按钮突出**

确保 `.price-view` 样式与购物车一致，按钮样式与整体设计系统一致。

---

### Task 9: 支付页优化

**Files:**
- Modify: `web/src/views/index/pay.vue`

**Changes:**

- [ ] **Step 1: 成功信息更大气，按钮样式一致**

在 `.pay-content` 样式中增强：

```less
.pay-content {
  text-align: center;
  padding: 60px 20px;

  .title {
    font-size: @font-size-3xl;
    color: @navy-dark;
    font-weight: 600;
    margin-bottom: 16px;
  }

  .price {
    font-size: @font-size-xl;
    color: @text-secondary;
    margin-bottom: 32px;

    .num {
      color: @primary-blue;
      font-weight: 600;
      font-size: @font-size-2xl;
    }
  }
}
```

---

## 验证方式

1. 启动前端开发服务器：`cd web && npm run dev`
2. 逐一访问各页面验证：
   - **首页**：Banner 轮播 dots 为圆角矩形，广告弹窗变为右侧 slide-in 卡片，产品卡片 hover 有 4px 上浮 + 阴影增强
   - **商品详情**：图片悬停放大，Tab 切换下划线动画流畅
   - **搜索**：左侧有筛选面板（可选扩展），结果卡片 hover 效果与首页一致
   - **购物车**：商品行 hover 有背景色变化，删除按钮 hover 变红，价格区域卡片化
   - **确认订单**：地址卡片 hover 边框颜色变化
   - **支付**：成功信息更突出，按钮样式一致
3. 检查响应式布局是否正常