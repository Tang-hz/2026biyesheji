# 首页产品列表与标签栏重构实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 对首页产品卡片、热门标签、KingKong分类导航进行 Apple Store 风格的现代化重构

**Architecture:** 修改 `content.vue` 中的 Less 样式，通过增强阴影、圆角、渐变和交互效果实现企业级视觉升级

**Tech Stack:** Vue 3, Less, Ant Design Vue, 现有 base.less 设计系统

---

## 文件修改概览

| 区域 | 文件 | 主要改动 |
|------|------|---------|
| 产品卡片 | `content.vue` (pc-thing-list 部分) | 图片高度提升、圆角 16px、阴影增强、hover 上浮 -6px |
| 快速操作按钮 | `content.vue` (quick-add-cart 部分) | 圆形 `+` → 胶囊形"加入购物车"文字按钮 |
| 热门标签 | `content.vue` (tag 部分) | 胶囊形、渐变背景 hover |
| KingKong 图标 | `content.vue` (kingkong-icon 部分) | 56px、渐变背景、弹跳 hover |

---

## 实现任务

### Task 1: 产品卡片重构

**Files:**
- Modify: `web/src/views/index/components/content.vue:684-728`

**Changes:**

- [ ] **Step 1: 修改 `.pc-thing-list .thing-item` 卡片样式**

```less
.pc-thing-list .thing-item {
  break-inside: avoid;
  margin-bottom: 16px;
  background: @white;
  border-radius: @radius-xl;  // 16px
  box-shadow: 0 4px 20px rgba(70, 132, 226, 0.08);
  overflow: hidden;
  cursor: pointer;
  display: inline-block;
  width: 100%;
  vertical-align: top;
  border: none;  // 移除边框，依靠阴影区分
  transition: transform @transition-base,
              box-shadow @transition-base;

  &:hover {
    transform: translateY(-6px);
    box-shadow: 0 12px 32px rgba(70, 132, 226, 0.15);
  }
}
```

- [ ] **Step 2: 修改 `.pc-thing-list .img-view img` 图片样式**

```less
.pc-thing-list .img-view {
  width: 100%;
  height: auto;
  line-height: 0;
  background: @bg-page;
  overflow: hidden;
}

.pc-thing-list .img-view img {
  width: 100%;
  height: auto;
  min-height: 180px;
  max-height: 260px;
  object-fit: cover;
  display: block;
  border-radius: @radius-xl @radius-xl 0 0;  // 16px top corners
  transition: transform @transition-slow;
}

.pc-thing-list .thing-item:hover .img-view img {
  transform: scale(1.05);
}
```

---

### Task 2: 快速操作按钮重构（圆形 `+` → 胶囊形）

**Files:**
- Modify: `web/src/views/index/components/content.vue:744-774`

**Changes:**

- [ ] **Step 1: 修改 `.pc-thing-list .quick-add-cart` 按钮样式**

```less
.pc-thing-list .quick-add-cart {
  flex-shrink: 0;
  height: 26px;
  padding: 0 12px;
  border: 1px solid @primary-blue;
  border-radius: @radius-full;
  background: transparent;
  color: @primary-blue;
  font-size: @font-size-sm;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all @transition-fast;

  &:hover {
    background: @primary-blue;
    color: @white;
    box-shadow: 0 2px 8px rgba(70, 132, 226, 0.3);
    transform: translateY(-1px);
  }

  &:active {
    background: @primary-blue-active;
    transform: translateY(0);
    box-shadow: @shadow-xs;
  }
}
```

- [ ] **Step 2: 更新模板中的按钮文字**（在 content.vue 第 114-117 行）

将 `+` 改为"加购"或"加入"文字

---

### Task 3: 热门标签重构

**Files:**
- Modify: `web/src/views/index/components/content.vue:535-564`

**Changes:**

- [ ] **Step 1: 修改 `.tag` 标签样式**

```less
.tag {
  background: @white;
  border: 1px solid @primary-blue;
  box-sizing: border-box;
  border-radius: 14px;  // 胶囊形
  height: 28px;
  line-height: 26px;
  padding: 0 14px;
  margin: 6px 8px 0 0;
  cursor: pointer;
  font-size: @font-size-sm;
  color: @primary-blue;
  font-weight: 500;
  transition: all @transition-fast;

  &:hover {
    background: linear-gradient(135deg, @primary-blue, @primary-blue-hover);
    color: @white;
    border-color: @primary-blue;
    box-shadow: 0 2px 8px rgba(70, 132, 226, 0.3);
    transform: translateY(-1px);
  }
}

.tag-select {
  background: linear-gradient(135deg, @primary-blue, @primary-blue-hover);
  color: @white;
  border: 1px solid @primary-blue;
  box-shadow: 0 2px 8px rgba(70, 132, 226, 0.3);
}
```

---

### Task 4: KingKong 分类导航重构

**Files:**
- Modify: `web/src/views/index/components/content.vue:442-488`

**Changes:**

- [ ] **Step 1: 修改 `.kingkong-item` 按钮样式**

```less
.kingkong-item {
  flex: 0 0 auto;
  width: 72px;  // 增加宽度
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  margin-right: 16px;
  padding: 4px 2px;
  border: none;
  background: transparent;
  cursor: pointer;
  font-family: inherit;
  transition: transform @transition-spring;
}
```

- [ ] **Step 2: 修改 `.kingkong-icon` 图标样式**

```less
.kingkong-icon {
  width: 56px;  // 48px → 56px
  height: 56px;  // 48px → 56px
  border-radius: @radius-xl;
  background: linear-gradient(145deg, @bg-input, @primary-blue-light);
  color: @primary-blue;
  font-size: 20px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 10px;
  transition: transform @transition-spring, box-shadow @transition-base, background @transition-fast;
  box-shadow: @shadow-sm;
}

.kingkong-item:hover .kingkong-icon {
  transform: translateY(-4px) scale(1.08);
  box-shadow: @shadow-lg;
  background: linear-gradient(145deg, @primary-blue-light, @bg-input);
}

.kingkong-item--active .kingkong-icon {
  background: linear-gradient(145deg, @primary-blue, @primary-blue-hover);
  color: @white;
  box-shadow: 0 6px 20px rgba(70, 132, 226, 0.4);
  transform: scale(1.08);
}
```

- [ ] **Step 3: 修改 `.kingkong-label` 文字样式**

```less
.kingkong-label {
  font-size: @font-size-sm;  // 11px → 12px
  color: @text-secondary;
  text-align: center;
  line-height: 1.3;
  max-width: 72px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color @transition-fast, font-weight @transition-fast;
}

.kingkong-item:hover .kingkong-label {
  color: @primary-blue;
  font-weight: 500;
}

.kingkong-item--active .kingkong-label {
  color: @primary-blue;
  font-weight: 600;
}
```

---

## 验证方式

1. 启动前端开发服务器：`cd web && npm run dev`
2. 访问首页验证：
   - **产品卡片**：hover 时向上浮动 6px，阴影明显加深，图片轻微放大
   - **快速操作按钮**：从圆形 `+` 变为胶囊形文字按钮，hover 时蓝色背景白色文字
   - **热门标签**：hover 时有蓝白渐变背景，白色文字
   - **KingKong 图标**：hover 时向上弹跳并轻微放大，选中时蓝色渐变背景
3. 检查响应式布局是否正常