# AI客服交互体验优化 — 设计文档

## 1. 背景与目标

**现状：**
- `consultation.vue` 使用 `marked` 库渲染 Markdown，自定义了三层表格解析（JSON表格 → 标准Markdown表格 → 宽松匹配）
- 链接显示原始 URL，图片不渲染，任务列表/代码高亮均不支持
- 表格解析逻辑复杂，与 marked 自身表格渲染存在竞争关系

**目标：**
- AI 回复混排文本 + Markdown 表格，前端统一通过 marked gfm 渲染，表格自动转为 HTML 表格展示
- 增强链接卡片、图片渲染、任务列表、代码高亮等交互体验
- 简化渲染管道，移除冗余的自定义表格解析逻辑

## 2. 技术方案

### 2.1 核心思路

保持 SSE 流式传输不变，后端 AI 输出标准 Markdown 文本，前端统一通过增强后的 marked 管道渲染。

```
后端 AI → Markdown 文本(SSE流式) → 前端 marked 渲染管道 → HTML
                                          ├── marked(gfm:true, tables:true)
                                          ├── marked-highlight (代码高亮)
                                          ├── marked-task-lists (任务列表)
                                          └── 自定义后处理 (链接卡片 + 图片放大)
```

### 2.2 marked 配置增强

**文件：** `consultation.vue`

```typescript
import { marked } from 'marked';
import { markedHighlight } from 'marked-highlight';
import hljs from 'highlight.js';
import { markedTaskLists } from 'marked-task-lists';

// 代码高亮
marked.use(markedHighlight({
  langPrefix: 'hljs language-',
  highlight(code, lang) {
    const language = hljs.getLanguage(lang) ? lang : 'plaintext';
    return hljs.highlight(code, { language }).value;
  }
}));

// 任务列表
marked.use(markedTaskLists({ enabled: true }));

// Markdown 解析
marked.use({
  gfm: true,
  breaks: true,
});
```

### 2.3 表格渲染

**策略：完全依赖 marked 自身 gfm 表格渲染，移除自定义三层解析逻辑**

- 删除 `parseJsonTable`、`parseMarkdownTable`、`parseFuzzyTable` 函数
- 删除 `renderTable` 自定义表格渲染函数
- AI 消息直接 `marked.parse(text)` 渲染
- 样式增强在 `<style scoped>` 中处理 table 样式

**备选：** 如 marked 表格渲染出现 bug（AI 输出不规范的 Markdown 表格），在 `renderMessage` 返回值后进行一次 `postProcess`，用正则提取并修复不规范表格。

### 2.4 链接卡片

**策略：渲染后 HTML 通过 DOM 后处理，将链接转换为卡片组件**

```typescript
const enhanceLinks = (html: string): string => {
  // 将孤立的长URL（未被 Markdown 链接语法包裹的URL）转换为链接卡片
  // 例如：https://example.com/product/123 → 链接卡片组件
  return html.replace(
    /<a href="(https?:\/\/[^"]+)"[^>]*>([^<]+)<\/a>/g,
    (match, url, text) => {
      try {
        const u = new URL(url);
        const domain = u.hostname.replace('www.', '');
        // 如果链接文字和域名不同，说明已经是 Markdown 链接，保留原样
        // 如果链接文字就是 URL 本身，转换为卡片
        if (text === url) {
          return `<div class="link-card" data-url="${url}" data-domain="${domain}">
            <div class="link-card-icon">🌐</div>
            <div class="link-card-body">
              <div class="link-card-title">${text}</div>
              <div class="link-card-url">${domain}</div>
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
```

**CSS 样式：**

```less
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

  .link-card-url {
    color: #a1adc5;
    font-size: 12px;
    margin-top: 2px;
  }
}
```

### 2.5 图片渲染

**策略：marked 直接将 `![](url)` 转为 `<img>` 标签，添加点击放大功能**

```typescript
// 在 bubble 的 .text div 上添加图片点击事件代理
// 通过 v-html 渲染后，用事件委托处理图片点击
const setupImageClick = () => {
  const historyEl = historyRef.value;
  if (!historyEl) return;

  historyEl.addEventListener('click', (e) => {
    const target = e.target as HTMLElement;
    if (target.tagName === 'IMG') {
      // 打开图片预览（使用 Ant Design 的 Image 组件或自定义预览）
      const src = (target as HTMLImageElement).src;
      window.open(src, '_blank');
    }
  });
};
```

**CSS 增强：**

```less
.text {
  img {
    max-width: 100%;
    border-radius: 8px;
    cursor: zoom-in;
    margin: 8px 0;
    transition: transform 0.2s;

    &:hover {
      transform: scale(1.02);
    }
  }
}
```

### 2.6 任务列表样式

```less
.text {
  // 任务列表
  ul[data-task-list] {
    list-style: none;
    padding-left: 0;

    li {
      display: flex;
      align-items: flex-start;
      gap: 8px;
      margin: 6px 0;
    }

    li::before {
      content: '⬜';
      font-size: 14px;
      margin-top: 2px;
    }

    li[data-task-list-item="checked"]::before {
      content: '✅';
    }
  }
}
```

### 2.7 代码高亮样式

引入 highlight.js 的 GitHub 主题：

```typescript
// 在 <style> 中引入
@import 'highlight.js/styles/github.css';
```

---

## 3. 消息结构

**不变：** 现有 SSE 流式传输架构保持不变，后端返回纯文本 Markdown。

```typescript
type ChatMessage = {
  id: number;
  role: 'user' | 'ai';
  text: string;          // Markdown 文本
  createTime: string;
  status?: 'thinking' | 'speaking' | 'done';
};
```

---

## 4. 渲染管道

```
SSE evt.data
    ↓
AI 消息累积（text += chunk）
    ↓
renderMessage(text, role)
    ↓
marked.parse(text)     // gfm + tables + breaks + highlight + task-lists
    ↓
enhanceLinks(html)     // URL → 链接卡片
    ↓
v-html 渲染
```

---

## 5. 文件变更清单

| 文件 | 变更内容 |
|------|----------|
| `web/src/views/index/consultation.vue` | 移除自定义表格解析函数；增强 marked 配置；添加链接卡片 + 图片点击 + 任务列表样式 |
| `web/package.json` | 添加 `highlight.js` 依赖（如果尚未引入） |

---

## 6. 验收标准

1. AI 输出 Markdown 混排文本 + 表格，前端正确渲染为 HTML 表格
2. AI 回复中的 URL 自动显示为链接卡片（而非裸露 URL）
3. AI 回复中的 `![](url)` 图片正确渲染并可点击放大
4. AI 输出任务列表（`- [ ] 任务`）正确显示复选框
5. AI 输出代码块有语法高亮
6. 流式响应体验不受影响
7. 页面样式与现有设计风格一致
