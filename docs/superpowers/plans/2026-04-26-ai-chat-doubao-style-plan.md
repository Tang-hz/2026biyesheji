# AI客服交互效果豆包化 — 实现计划

> **Goal:** 后端输出结构化 JSON 块，前端解析并渲染为 HTML 表格，实现纯文本、表格、混合内容的优雅展示。

---

## 一、变更文件清单

| 文件 | 变更 |
|------|------|
| `server/src/main/resources/Prompts/SystemPrompt.txt` | 新增「输出格式规则」章节 |
| `server/src/main/java/com/gk/study/ai/AiReactService.java` | `@SystemMessage` 改为 `fromResource` 加载外部文件 |
| `web/src/views/index/consultation.vue` | 新增 JSON 块解析逻辑 + 样式增强 |

---

## Task 1: 更新 SystemPrompt.txt — 新增输出格式规则

**Files:**
- Modify: `server/src/main/resources/Prompts/SystemPrompt.txt`

- [ ] **Step 1: 在 SystemPrompt.txt 末尾追加以下内容**

在文件末尾（`服务闭环要求` 章节之后，文件最底部）添加：

```markdown
---

## 输出格式规则

### 结构化数据输出（列表类问题）

当需要输出列表类内容时（商品列表、订单列表、推荐商品等），必须使用 JSON 块格式：

___JSON_START___
{"type":"table","title":"标题","columns":["列1","列2"],"rows":[["值1","值2"],["值3","值4"]]}
___JSON_END___

**JSON 块规则：**
- `___JSON_START___` 和 `___JSON_END___` 是固定分隔符，禁止省略
- JSON 内部字段：`type` 必填（固定为 "table"），`title` 可选，`columns` 必填（数组），`rows` 必填（二维数组）
- 禁止在 JSON 中包含 Markdown 表格语法
- 表格内容禁止 emoji

### 文本与 JSON 块混合输出

文本和 JSON 块可以混合输出，例如：
```
为您找到了以下商品：
___JSON_START___{"type":"table","title":"商品列表","columns":["名称","价格"],"rows":[["书1","¥32"]]}___JSON_END___
请问您想购买哪一款呢？
```

### 非列表类问题

直接输出纯文本，不需要 JSON 块。

### 示例

**正确输出（列表类）：**
您好！我帮您找到以下计算机类图书：

___JSON_START___{"type":"table","title":"计算机类图书","columns":["序号","书名","价格","库存"],"rows":[["1","SQL入门经典（第5版）","¥324.00","555本"],["2","TCP/IP入门经典（第5版）","¥56.00","777本"]]}___JSON_END___

如需购买，请告诉我书名。

**正确输出（非列表类）：**
SQL入门经典（第5版）的价格是 ¥324.00，目前库存充足。

**错误输出（禁止）：**
- Markdown 表格格式：| 书名 | 价格 |（这类内容应改为 JSON 块）
- 无分隔符的 JSON：{"type":"table"...}（缺少 ___JSON_START___ 和 ___JSON_END___）
- JSON 内含 emoji："rows":[["书名","💰"]]
```

- [ ] **Step 2: 提交**

```bash
cd java_shop-master/java_shop-master
git add server/src/main/resources/Prompts/SystemPrompt.txt
git commit -m "docs(ai): add structured output format rules to SystemPrompt"
```

---

## Task 2: 修改 AiReactService.java — 改为加载外部文件

**Files:**
- Modify: `server/src/main/java/com/gk/study/ai/AiReactService.java:1-72`

- [ ] **Step 1: 修改 @SystemMessage 注解**

将当前的：
```java
@SystemMessage("""
你是一个电商平台的AI客服。
...
""")
```

替换为：
```java
@SystemMessage(fromResource = "Prompts/SystemPrompt.txt")
```

- [ ] **Step 2: 删除不再需要的内联 SystemMessage 内容**

删除所有内联的 SystemMessage 字符串（从第 18 行左右开始的大段文字）。

- [ ] **Step 3: 提交**

```bash
git add server/src/main/java/com/gk/study/ai/AiReactService.java
git commit -m "refactor(ai): load SystemMessage from external file SystemPrompt.txt"
```

---

## Task 3: 前端 JSON 块解析逻辑

**Files:**
- Modify: `web/src/views/index/consultation.vue`

- [ ] **Step 1: 新增 JSON 块解析相关变量和函数**

在 `<script setup>` 中添加：

```typescript
// JSON 块缓冲：当检测到 JSON_START 时，进入 buffer 模式直到 JSON_END
const jsonBuffer = ref('');

// 检测 JSON 块边界，返回 { text, jsonBlock }
const detectJsonBlock = (chunk: string): { text: string; jsonBlock: string | null } => {
  // 如果当前在 buffer 模式，追加内容并检测 JSON_END
  if (jsonBuffer.value) {
    jsonBuffer.value += chunk;
    const jsonEndIdx = jsonBuffer.value.indexOf('___JSON_END___');
    if (jsonEndIdx !== -1) {
      const jsonContent = jsonBuffer.value.substring(0, jsonEndIdx);
      jsonBuffer.value = '';
      return { text: '', jsonBlock: jsonContent };
    }
    return { text: '', jsonBlock: null };
  }

  // 检测 JSON_START
  const jsonStartIdx = chunk.indexOf('___JSON_START___');
  if (jsonStartIdx !== -1) {
    const beforeJson = chunk.substring(0, jsonStartIdx);
    const afterStart = chunk.substring(jsonStartIdx + '___JSON_START___'.length);
    const jsonEndIdx = afterStart.indexOf('___JSON_END___');
    if (jsonEndIdx !== -1) {
      const jsonContent = afterStart.substring(0, jsonEndIdx);
      const afterJson = afterStart.substring(jsonEndIdx + '___JSON_END___'.length);
      if (afterJson) jsonBuffer.value = afterJson;
      return { text: beforeJson, jsonBlock: jsonContent };
    } else {
      jsonBuffer.value = afterStart;
      return { text: beforeJson, jsonBlock: null };
    }
  }

  return { text: chunk, jsonBlock: null };
};

// 解析 JSON 表格为 HTML
const parseTableJson = (jsonStr: string): string => {
  try {
    const data = JSON.parse(jsonStr);
    if (data.type !== 'table') return '';
    const { title, columns, rows } = data;
    const titleHtml = title ? `<div class="ai-table-title">${escapeHtml(title)}</div>` : '';
    const headerCells = (columns ?? []).map(col => `<th>${escapeHtml(col)}</th>`).join('');
    const bodyRows = (rows ?? []).map((row, idx) => {
      const cells = row.map(cell => `<td>${escapeHtml(cell)}</td>`).join('');
      return `<tr>${cells}</tr>`;
    }).join('');
    return `<div class="ai-table">${titleHtml}<table><thead><tr>${headerCells}</tr></thead><tbody>${bodyRows}</tbody></table></div>`;
  } catch {
    return '';
  }
};
```

- [ ] **Step 2: 修改 SSE onmessage 事件处理**

找到 `es.onmessage = (evt) => { ... }` 部分，替换为：

```typescript
es.onmessage = (evt) => {
  const chunk = evt.data ?? '';
  const { text, jsonBlock } = detectJsonBlock(chunk);
  const msg = chatMessages.value.find((m) => m.id === aiMsgId);
  if (msg) {
    if (msg.status === 'thinking') msg.status = 'speaking';
    if (text) {
      msg.text += text;
    }
    if (jsonBlock) {
      msg.text += parseTableJson(jsonBlock);
    }
  }
  scrollToBottom();
};
```

- [ ] **Step 3: 提交**

```bash
git add web/src/views/index/consultation.vue
git commit -m "feat(consultation): add JSON block parsing for structured table output"
```

---

## Task 4: 样式增强

**Files:**
- Modify: `web/src/views/index/consultation.vue` (style section)

- [ ] **Step 1: 在 `.text` 样式块后添加表格样式**

在 `</style>` 之前添加：

```less
// AI 表格样式
.text {
  :deep(.ai-table) {
    margin: 12px 0;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
    border: 1px solid #e8ecf0;
  }
  :deep(.ai-table-title) {
    background: linear-gradient(135deg, #4684e2 0%, #5a9aed 100%);
    color: #fff;
    font-weight: 600;
    padding: 10px 16px;
    font-size: 14px;
    border-radius: 8px 8px 0 0;
  }
  :deep(.ai-table table) {
    width: 100%;
    border-collapse: collapse;
  }
  :deep(.ai-table th) {
    background: #f0f5ff;
    color: #152844;
    font-weight: 600;
    padding: 10px 16px;
    text-align: left;
    border-bottom: 1px solid #e8ecf0;
  }
  :deep(.ai-table td) {
    padding: 10px 16px;
    border-bottom: 1px solid #f0f0f0;
    color: #333;
  }
  :deep(.ai-table tr:last-child td) {
    border-bottom: none;
  }
  :deep(.ai-table tr:hover td) {
    background: #f8faff;
  }
  :deep(.ai-table tr:nth-child(even) td) {
    background: #fafcff;
  }
}
```

- [ ] **Step 2: 提交**

```bash
git add web/src/views/index/consultation.vue
git commit -m "style(consultation): add AI table styles with gradient header"
```

---

## Task 5: 最终验证

**Files:**
- None (验证任务)

- [ ] **Step 1: 后端编译验证**

```bash
cd java_shop-master/java_shop-master/server
mvn compile -q
```
Expected: BUILD SUCCESS

- [ ] **Step 2: 前端编译验证**

```bash
cd java_shop-master/java_shop-master/web
npm run build 2>&1 | head -30
```
Expected: 无编译错误

- [ ] **Step 3: 整体检查**

1. ✅ `SystemPrompt.txt` 末尾新增了「输出格式规则」章节
2. ✅ `AiReactService.java` 的 `@SystemMessage` 改为 `fromResource`
3. ✅ `consultation.vue` 新增 `detectJsonBlock` 和 `parseTableJson` 函数
4. ✅ SSE 处理逻辑已修改为解析 JSON 块
5. ✅ `.ai-table` 样式已添加

- [ ] **Step 4: 提交所有变更**

```bash
git add -A
git commit -m "feat(ai): implement structured JSON output with HTML table rendering"
```

---

## 验收标准自检

| 验收项 | 检查方法 |
|--------|---------|
| 非列表类问题显示文本 | 问"SQL入门经典多少钱？"，观察纯文本输出 |
| 列表类问题显示表格 | 问"有哪些计算机类图书？"，观察 JSON 表格渲染 |
| 混合内容正确展示 | 问"推荐几个商品"，观察文本+表格混合 |
| 流式打字机效果正常 | 观察输出过程无闪烁、无断裂 |
| JSON 块分隔符正确 | F12 开发者工具查看网络请求中 `___JSON_START___` 出现 |