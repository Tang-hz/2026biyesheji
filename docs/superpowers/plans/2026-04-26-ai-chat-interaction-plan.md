# AI客服交互体验优化 — 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 增强 consultation.vue 的 marked 渲染管道，实现链接卡片、图片点击放大、任务列表、代码高亮；移除冗余的自定义表格解析逻辑。

**Architecture:** 保持 SSE 流式传输不变，后端输出标准 Markdown，前端通过增强后的 marked 管道统一渲染为 HTML。移除三层自定义表格解析，依赖 marked 自身 gfm 表格渲染。

**Tech Stack:** marked v9.1.6, highlight.js, marked-highlight, marked-task-lists, Vue 3

---

## 变更文件清单

| 文件 | 变更 |
|------|------|
| `web/package.json` | 添加 `highlight.js`、`marked-highlight`、`marked-task-lists` 依赖 |
| `web/src/views/index/consultation.vue` | 移除自定义表格解析；增强 marked 配置；添加链接卡片 + 图片点击 + 任务列表 + 代码高亮 |
| `web/src/views/index/consultation.vue` (style) | 添加链接卡片、图片、任务列表、代码块的 CSS 样式 |

---

## Task 1: 添加 highlight.js 依赖

**Files:**
- Modify: `web/package.json:10`

- [ ] **Step 1: 修改 package.json，添加 highlight.js 依赖**

将 `highlight.js` 添加到 `dependencies`：

```json
"dependencies": {
  "@ant-design/icons-vue": "^6.1.0",
  "@vueuse/components": "^9.10.0",
  "@vueuse/core": "^9.10.0",
  "ant-design-vue": "^3.2.20",
  "axios": "^1.2.2",
  "highlight.js": "^11.9.0",
  "marked": "^9.1.6",
  "marked-highlight": "^2.1.1",
  "marked-task-lists": "^1.0.0",
  "pinia": "^2.0.28",
  "pinia-plugin-persistedstate": "^3.0.2",
  "qs": "^6.11.0",
  "vue": "^3.2.45",
  "vue-router": "^4.1.6"
}
```

- [ ] **Step 2: 安装新依赖**

Run: `cd web && npm install`

Expected: highlight.js、marked-highlight、marked-task-lists 安装成功

- [ ] **Step 3: 提交**

```bash
git add web/package.json web/package-lock.json
git commit -m "deps(consultation): add highlight.js, marked-highlight, marked-task-lists"
```

---

## Task 2: 移除自定义表格解析逻辑

**Files:**
- Modify: `web/src/views/index/consultation.vue:104-209`

- [ ] **Step 1: 删除以下函数**

从 `consultation.vue` 中删除：
- `parseJsonTable` (约 line 105-117)
- `isSeparatorLine` (约 line 119-124)
- `parseFuzzyTable` (约 line 127-164)
- `parseMarkdownTable` (约 line 167-209)
- `renderTable` (约 line 249-278)

这些函数的定义在文件的 `<script setup>` 部分，找到对应行后删除。

- [ ] **Step 2: 提交**

```bash
git add web/src/views/index/consultation.vue
git commit -m "refactor(consultation): remove custom table parsing logic"
```

---

## Task 3: 重构 marked 配置

**Files:**
- Modify: `web/src/views/index/consultation.vue:85-103`

- [ ] **Step 1: 更新 marked 配置代码**

将现有的：

```typescript
// 配置marked
marked.setOptions({
  breaks: true,
  gfm: true,
});
```

替换为：

```typescript
import { marked } from 'marked';
import { markedHighlight } from 'marked-highlight';
import hljs from 'highlight.js';
import { markedTaskLists } from 'marked-task-lists';

// 配置 marked 代码高亮
marked.use(markedHighlight({
  langPrefix: 'hljs language-',
  highlight(code, lang) {
    const language = hljs.getLanguage(lang) ? lang : 'plaintext';
    return hljs.highlight(code, { language }).value;
  }
}));

// 配置任务列表
marked.use(markedTaskLists({ enabled: true }));

// 配置 marked 基本选项
marked.use({
  gfm: true,
  breaks: true,
});
```

- [ ] **Step 2: 提交**

```bash
git add web/src/views/index/consultation.vue
git commit -m "feat(consultation): configure marked with highlight.js and task lists"
```

---

## Task 4: 重构 renderMessage 函数

**Files:**
- Modify: `web/src/views/index/consultation.vue:217-246`

- [ ] **Step 1: 替换 renderMessage 函数**

将现有的 `renderMessage` 函数（包含三层表格检测逻辑）替换为：

```typescript
// 渲染消息文本
const renderMessage = (text: string, role: 'user' | 'ai'): string => {
  if (!text) return '';

  // 用户消息直接返回纯文本
  if (role === 'user') {
    return escapeHtml(text);
  }

  // AI 消息：marked 渲染 Markdown
  let html = marked.parse(text) as string;

  // 增强链接：将裸露 URL 转换为链接卡片
  html = enhanceLinks(html);

  return html;
};

// HTML 实体转义
const escapeHtml = (str: string): string => {
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
};

// 增强链接：将裸露 URL 转换为链接卡片
const enhanceLinks = (html: string): string => {
  return html.replace(
    /<a href="(https?:\/\/[^"]+)"[^>]*>([^<]+)<\/a>/g,
    (match, url, text) => {
      try {
        const u = new URL(url);
        const domain = u.hostname.replace('www.', '');
        // 如果链接文字就是 URL 本身，转换为卡片
        if (text === url) {
          return `<div class="link-card" data-url="${escapeHtmlAttr(url)}" onclick="window.open('${escapeHtmlAttr(url)}', '_blank')">
            <div class="link-card-icon">🌐</div>
            <div class="link-card-body">
              <div class="link-card-title">${escapeHtml(text)}</div>
              <div class="link-card-domain">${escapeHtml(domain)}</div>
            </div>
          </div>`;
        }
        return match;
      } catch {
        return match;
      }
    }
  );
};

// 属性值转义（用于 data-* 属性）
const escapeHtmlAttr = (str: string): string => {
  return str
    .replace(/&/g, '&amp;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
};
```

**注意：** 删除了原来的 `renderMarkdown` 函数和 `escapeHtml` 函数定义（已在上面重写）。

- [ ] **Step 2: 验证编译**

Run: `cd web && npm run build 2>&1 | head -50`

Expected: 无编译错误

- [ ] **Step 3: 提交**

```bash
git add web/src/views/index/consultation.vue
git commit -m "feat(consultation): simplify renderMessage to use marked directly with link enhancement"
```

---

## Task 5: 添加链接卡片、图片、任务列表、代码块样式

**Files:**
- Modify: `web/src/views/index/consultation.vue:638-684`

- [ ] **Step 1: 在 `.text` 样式块后添加新样式**

在 `<style scoped lang="less">` 中，找到 `.text { ... }` 样式块（line 638-684），在 `}` 后添加：

```less
// 链接卡片
.link-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #f6f9fb;
  border: 1px solid #e9f0fb;
  border-radius: 8px;
  margin: 8px 0;
  cursor: pointer;
  transition: background 0.2s;

  &:hover {
    background: #ecf3fc;
  }

  .link-card-icon {
    font-size: 24px;
    flex-shrink: 0;
  }

  .link-card-body {
    flex: 1;
    min-width: 0;
  }

  .link-card-title {
    color: #152844;
    font-size: 14px;
    font-weight: 500;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .link-card-domain {
    color: #a1adc5;
    font-size: 12px;
    margin-top: 2px;
  }
}

// 图片样式
.text {
  img {
    max-width: 100%;
    max-height: 300px;
    border-radius: 8px;
    cursor: zoom-in;
    margin: 8px 0;
    transition: transform 0.2s;
    object-fit: contain;

    &:hover {
      transform: scale(1.02);
    }
  }
}

// 任务列表样式
.text {
  ul[data-task-list] {
    list-style: none;
    padding-left: 0;
    margin: 8px 0;

    li {
      display: flex;
      align-items: flex-start;
      gap: 8px;
      margin: 6px 0;
      color: #152844;
      font-size: 14px;
    }

    li::before {
      content: '⬜';
      font-size: 14px;
      margin-top: 2px;
      flex-shrink: 0;
    }

    li[data-task-list-item="checked"]::before {
      content: '✅';
    }
  }
}

// 代码块样式
.text {
  pre {
    background: #f5f5f5;
    border-radius: 6px;
    padding: 12px;
    overflow-x: auto;
    margin: 10px 0;

    code {
      background: none;
      padding: 0;
      border-radius: 0;
      font-size: 13px;
      color: #24292e;
    }
  }

  code {
    background: #f0f0f0;
    padding: 2px 6px;
    border-radius: 4px;
    font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
    font-size: 13px;
    color: #24292e;
  }
}

// 引用块样式
.text {
  blockquote {
    margin: 10px 0;
    padding: 8px 16px;
    border-left: 3px solid #4684e2;
    background: #f6f9fb;
    color: #555;
    border-radius: 0 4px 4px 0;

    p {
      margin: 0;
    }
  }
}
```

- [ ] **Step 2: 在组件卸载时移除事件监听**

在 `<script setup>` 中找到 `onMounted` 部分，确保图片点击事件使用事件委托方式处理，无需单独添加监听器（已在 `onclick` 内联处理）。

- [ ] **Step 3: 验证样式编译**

Run: `cd web && npm run build 2>&1 | head -30`

Expected: 无 Less 编译错误

- [ ] **Step 4: 提交**

```bash
git add web/src/views/index/consultation.vue
git commit -m "feat(consultation): add styles for link cards, images, task lists, code blocks, and blockquotes"
```

---

## Task 6: 最终验证

**Files:**
- None (验证任务)

- [ ] **Step 1: 构建验证**

Run: `cd web && npm run build`

Expected: BUILD SUCCESS，无错误

- [ ] **Step 2: 整体检查**

确认以下变更已完成：

1. ✅ `highlight.js`、`marked-highlight`、`marked-task-lists` 依赖已添加
2. ✅ 自定义 `parseJsonTable`、`parseMarkdownTable`、`parseFuzzyTable`、`renderTable` 函数已删除
3. ✅ `marked` 配置已增强（代码高亮 + 任务列表）
4. ✅ `renderMessage` 已简化为 marked 直接渲染 + enhanceLinks
5. ✅ CSS 样式已添加（链接卡片、图片、任务列表、代码块、引用块）

- [ ] **Step 3: 提交所有变更**

```bash
git add -A
git commit -m "feat(consultation): enhance AI chat rendering with link cards, image zoom, task lists, and code highlighting"
```

---

## 验收标准自检

| 验收项 | 检查方法 |
|--------|----------|
| AI 输出 Markdown 混排文本+表格正确渲染 | 启动前端，发送"推荐几个商品"等触发表格的 query |
| 链接显示为卡片而非裸露 URL | 发送包含 `https://` 链接的 query |
| 图片可点击放大 | 发送包含 `![](url)` 的 query |
| 任务列表显示复选框 | 发送 `- [ ] 任务1\n- [x] 任务2` |
| 代码块有语法高亮 | 发送"写一个快速排序算法" |
| 流式响应体验不受影响 | 观察打字机效果是否正常 |
| 样式风格一致 | 对比现有设计，无突兀变化 |

---

## 潜在问题与备选方案

**问题1: marked 表格渲染出现 bug**

如果 AI 输出的 Markdown 表格不规范（如缺少分隔行 `|---|`），marked 可能渲染失败。

**备选：** 在 `renderMessage` 返回前，增加一次后处理正则，对遗漏的表格进行修复：

```typescript
// 后处理：尝试修复不规范的表格
const postProcessTables = (html: string): string => {
  // 匹配 2 行以上的 | 分隔文本，转换为表格
  // ...
};
```

**问题2: marked-highlight / marked-task-lists 与 marked 9.x 兼容性**

**备选：** 如有兼容问题，降级到手动处理：
- 代码高亮：直接引入 highlight.js，在 marked renderer 中手动调用
- 任务列表：自定义 marked 扩展

---

## 执行选项

**Plan complete and saved to `docs/superpowers/plans/2026-04-26-ai-chat-interaction-plan.md`.**

**Two execution options:**

**1. Subagent-Driven (recommended)** - I dispatch a fresh subagent per task, review between tasks, fast iteration

**2. Inline Execution** - Execute tasks in this session using executing-plans, batch execution with checkpoints

**Which approach?**
