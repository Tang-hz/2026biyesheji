# AI 客服表格渲染优化实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** AI 客服返回 JSON 表格数据时，前端渲染为工整的 Ant Design `<a-table>`；后端通过 system prompt 指示 AI 输出 JSON 格式而非 markdown 表格。

**Architecture:** 前端在消息渲染时检测 JSON 表格格式，解析后用 Vue 的 `h()` 函数渲染 `<a-table>` 组件；后端通过 `@SystemMessage` 注解为 AI 添加输出格式说明。

**Tech Stack:** Vue 3 (Composition API) + Ant Design Vue (`<a-table>`) + LangChain4j (`@AiService` + `@SystemMessage`)

---

## 变更文件清单

| 文件 | 改动 |
|------|------|
| `java_shop-master/java_shop-master/server/src/main/java/com/gk/study/ai/AiReactService.java` | 添加 `@SystemMessage` 注解，给 AI 输入 JSON 表格输出格式说明 |
| `java_shop-master/java_shop-master/web/src/views/index/consultation.vue` | 新增表格检测/解析函数，修改渲染逻辑 |

---

## 实施步骤

---

### Task 1: 后端 — 为 AiReactService 添加 System Prompt

**Files:**
- Modify: `java_shop-master/java_shop-master/server/src/main/java/com/gk/study/ai/AiReactService.java:1-29`

- [ ] **Step 1: 添加 @SystemMessage 注解**

在 `AiReactService` 接口声明前添加 `@SystemMessage` 注解，内容如下：

```java
@SystemMessage("""
你是一个专业的电商平台 AI 客服。当需要展示列表或表格数据（如商品列表、订单详情、积分明细等）时，必须使用以下 JSON 格式返回，不要使用 markdown 表格或其他文本格式：

{"type":"table","title":"可选的表格标题","columns":["列名1","列名2"],"rows":[["值1","值2"],["值3","值4"]]}

示例：
用户问："有哪些计算机类图书"
正确回答（仅返回 JSON，不要有其他解释性文字）：
{"type":"table","title":"计算机类图书","columns":["序号","书名","原价","会员价","库存"],"rows":[["1","SQL入门经典","¥324.00","¥275.40","555本"],["2","TCP/IP入门经典","¥56.00","¥47.60","777本"]]}

注意事项：
- 始终使用 {"type":"table"} 结构
- columns 是表头数组
- rows 是二维数组，每行一个数组
- 不要在 JSON 前后添加任何 markdown 表格符号（|、- 等）
- 非列表类问题正常回答即可
""")
@AiService
public interface AiReactService {
    Flux<String> chat(@MemoryId String memoryId, @UserMessage String userMessage);
}
```

- [ ] **Step 2: 验证编译**

```bash
cd java_shop-master/java_shop-master/server && mvn compile -q
```

预期：编译成功，无报错

- [ ] **Step 3: 提交**

```bash
git add server/src/main/java/com/gk/study/ai/AiReactService.java
git commit -m "feat(ai): 添加 system prompt 指示 AI 输出 JSON 表格格式"
```

---

### Task 2: 前端 — 修改 consultation.vue 渲染逻辑

**Files:**
- Modify: `java_shop-master/java_shop-master/web/src/views/index/consultation.vue`

- [ ] **Step 1: 在 `<script setup>` 中新增辅助函数**

在 `marked.setOptions` 之后、`renderMarkdown` 函数之前添加：

```typescript
// 检测消息是否为 JSON 表格格式
const isTableMessage = (text: string): boolean => {
  const trimmed = text.trim();
  if (!trimmed.startsWith('{')) return false;
  try {
    const parsed = JSON.parse(trimmed);
    return parsed && parsed.type === 'table' && Array.isArray(parsed.columns) && Array.isArray(parsed.rows);
  } catch {
    return false;
  }
};

// 解析 JSON 表格消息
const parseTableMessage = (text: string): { title?: string; columns: string[]; rows: string[][] } | null => {
  try {
    const parsed = JSON.parse(text.trim());
    if (parsed && parsed.type === 'table') {
      return {
        title: parsed.title || '',
        columns: parsed.columns || [],
        rows: parsed.rows || [],
      };
    }
  } catch {}
  return null;
};
```

- [ ] **Step 2: 修改 bubble 中的渲染逻辑**

找到第 62 行：
```vue
<div class="text" v-html="item.role === 'ai' ? renderMarkdown(item.text) : item.text"></div>
```

替换为：
```vue
<div class="text" v-html="renderMessage(item.text, item.role)"></div>
```

- [ ] **Step 3: 修改 renderMarkdown 函数，增加 JSON 检测分支**

将 `renderMarkdown` 函数修改为 `renderMessage` 函数：

```typescript
// 渲染消息文本
const renderMessage = (text: string, role: 'user' | 'ai'): string => {
  if (!text) return '';

  // AI 消息优先检测 JSON 表格格式
  if (role === 'ai' && isTableMessage(text)) {
    return renderTable(parseTableMessage(text)!);
  }

  // 其他 AI 消息走原有 markdown 渲染
  if (role === 'ai') {
    return renderMarkdown(text);
  }

  // 用户消息直接返回
  return text;
};
```

- [ ] **Step 4: 新增 renderTable 函数**

在 `renderMarkdown` 函数之后添加：

```typescript
// 渲染 JSON 表格为 HTML
const renderTable = (data: { title?: string; columns: string[]; rows: string[][] }): string => {
  const { title, columns, rows } = data;

  // 生成表头
  const headerCells = columns
    .map((col) => `<th style="background:#e8eff7;color:#152844;font-size:14px;padding:8px 12px;border:1px solid #ddd;text-align:left;font-weight:600;">${escapeHtml(col)}</th>`)
    .join('');

  // 生成数据行
  const bodyRows = rows
    .map(
      (row, rowIndex) =>
        `<tr style="background:${rowIndex % 2 === 0 ? '#fff' : '#fafafa'};">` +
        row.map((cell) => `<td style="padding:8px 12px;border:1px solid #ddd;color:#152844;font-size:14px;">${escapeHtml(cell)}</td>`).join('') +
        '</tr>',
    )
    .join('');

  const titleHtml = title ? `<div style="font-weight:600;color:#152844;font-size:14px;margin-bottom:8px;">${escapeHtml(title)}</div>` : '';

  return `
    <div style="margin:8px 0;overflow-x:auto;">
      ${titleHtml}
      <table style="border-collapse:collapse;width:100%;font-size:14px;table-layout:auto;">
        <thead><tr>${headerCells}</tr></thead>
        <tbody>${bodyRows}</tbody>
      </table>
    </div>
  `;
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
```

- [ ] **Step 5: 删除旧的 renderMarkdown 函数（已迁移到 renderMessage）**

找到旧的 `renderMarkdown` 函数，将其重命名/修改为上述新逻辑后，删除原来的版本。

注意：保留 `marked.setOptions` 块和 `marked.parse` 调用，只是整合到 `renderMessage` 中。

修改后的 `renderMarkdown` 函数（保留兼容）：

```typescript
// 保留原有 marked 渲染，供 renderMessage 内部调用
const renderMarkdown = (text: string): string => {
  if (!text) return '';
  return marked.parse(text) as string;
};
```

- [ ] **Step 6: 添加表格 hover 样式**

在现有的 `.text` style 块中，已有的 `tr:hover` 样式会自动对动态插入的表格生效。确认 `.text` 样式块中包含以下内容（已存在于 consultation.vue 第 481-483 行）：

```css
tr:hover {
  background: #f0f7ff;
}
```

- [ ] **Step 7: 验证前端编译**

```bash
cd java_shop-master/java_shop-master/web && npm run build 2>&1 | head -30
```

预期：编译成功，无 Error（警告可忽略）

- [ ] **Step 8: 提交**

```bash
git add web/src/views/index/consultation.vue
git commit -m "feat(consultation): 支持 JSON 表格渲染，优化 AI 客服列表展示"
```

---

## 验证测试

测试流程：

1. 启动后端和前端
2. 登录客服页面，向 AI 提问："有哪些计算机类图书？"或"查看商品列表"
3. AI 返回 JSON 格式时，表格应在聊天气泡内以工整的 `<a-table>` 样式展示
4. 非表格问题（如"你好"）仍正常回复

---

## 补充说明

- **JSON 检测策略**：仅检测以 `{` 开头的消息，防止误判普通文本
- **错误处理**：JSON 解析失败时自动回退到 markdown 渲染
- **XSS 防护**：`escapeHtml()` 对所有单元格内容进行转义
- **样式继承**：表格样式与 admin 页面一致，使用项目已有设计系统
