# AI客服交互效果豆包化 — 设计文档

> **目标：** 后端输出结构化数据（JSON 表格块），前端解析并渲染为 HTML 表格，实现纯文本、表格、混合内容的优雅展示，效果对标豆包。

---

## 一、整体架构

```
用户输入
  ↓
后端 SSE 流式输出（非列表类→纯文本，列表类→JSON块）
  ↓
前端 consultation.vue 解析流式内容
  ├─ 普通文本 → 直接追加显示（打字机效果）
  └─ JSON 块（___JSON_START___ ... ___JSON_END___）→ 解析为 HTML 表格并渲染
```

### 流式输出协议

| 内容类型 | 后端输出格式 | 前端渲染方式 |
|---------|-------------|-------------|
| 普通文本 | 直接流式输出（如 "您好，请问有什么"） | 追加到 bubble 末尾 |
| 表格 JSON | `___JSON_START___` + JSON + `___JSON_END___` | 解析 JSON，插入 HTML 表格 |

### JSON 表格块格式

```json
{
  "type": "table",
  "title": "商品列表",
  "columns": ["名称", "价格", "库存"],
  "rows": [
    ["SQL入门经典（第5版）", "¥324.00", "555本"],
    ["TCP/IP入门经典（第5版）", "¥56.00", "777本"]
  ]
}
```

### 混合内容示例

后端流式输出：
```
为您找到了以下商品：
___JSON_START___{"type":"table","title":"商品列表","columns":["名称","价格"],"rows":[["书1","¥32"]]}___JSON_END___
请问您想购买哪一款呢？
```

前端渲染效果：
```
为您找到了以下商品：

┌────────────────────────────┐
│       商品列表              │
├──────┬───────┬──────────────┤
│ 名称 │ 价格  │ 库存          │
├──────┼───────┼──────────────┤
│ 书1  │ ¥32   │ 充足          │
└──────┴───────┴──────────────┘

请问您想购买哪一款呢？
```

---

## 二、后端改造

### 2.1 修改 SystemPrompt.txt

在 `server/src/main/resources/Prompts/SystemPrompt.txt` 末尾新增：

```markdown
---

## 输出格式规则

### 结构化数据输出（列表类问题）

当需要输出列表类内容时（商品列表、订单列表、推荐商品等），必须使用 JSON 块格式：

```
___JSON_START___
{"type":"table","title":"标题","columns":["列1","列2"],"rows":[["值1","值2"],["值3","值4"]]}
___JSON_END___
```

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
```
您好！我帮您找到以下计算机类图书：

___JSON_START___{"type":"table","title":"计算机类图书","columns":["序号","书名","价格","库存"],"rows":[["1","SQL入门经典（第5版）","¥324.00","555本"],["2","TCP/IP入门经典（第5版）","¥56.00","777本"]]}___JSON_END___

如需购买，请告诉我书名。
```

**正确输出（非列表类）：**
```
SQL入门经典（第5版）的价格是 ¥324.00，目前库存充足。
```

**错误输出（禁止）：**
- Markdown 表格格式：`| 书名 | 价格 |`（这类内容应改为 JSON 块）
- 无分隔符的 JSON：`{"type":"table"...}`（缺少 `___JSON_START___` 和 `___JSON_END___`）
- JSON 内含 emoji：`"rows":[["书名","💰"]]`
```

### 2.2 修改 AiReactService.java

移除内联 `@SystemMessage`，改为加载外部文件：

```java
@SystemMessage(fromResource = "Prompts/SystemPrompt.txt")
@AiService
public interface AiReactService {
    Flux<String> chat(@MemoryId String memoryId, @UserMessage String userMessage);
}
```

`fromResource` 是 LangChain4j 内置支持的，会从 classpath 加载文件内容。

---

## 三、前端改造

### 3.1 修改 consultation.vue

#### 新增状态和解析逻辑

```typescript
// 流式内容缓冲：当检测到 JSON_START 时，进入 buffer 模式直到 JSON_END
const contentBuffer = ref('');

// 检测 JSON 块边界
const detectJsonBlock = (chunk: string): { text: string; jsonBlock: string | null } => {
  // 如果当前在 buffer 模式，追加内容并检测 JSON_END
  if (contentBuffer.value) {
    contentBuffer.value += chunk;
    const jsonEndIdx = contentBuffer.value.indexOf('___JSON_END___');
    if (jsonEndIdx !== -1) {
      const jsonContent = contentBuffer.value.substring(0, jsonEndIdx);
      contentBuffer.value = '';
      return { text: '', jsonBlock: jsonContent };
    }
    return { text: '', jsonBlock: null };
  }

  // 检测 JSON_START
  const jsonStartIdx = chunk.indexOf('___JSON_START___');
  if (jsonStartIdx !== -1) {
    // 分割：JSON_START 前的文本 + JSON 内容
    const beforeJson = chunk.substring(0, jsonStartIdx);
    const afterStart = chunk.substring(jsonStartIdx + '___JSON_START___'.length);
    const jsonEndIdx = afterStart.indexOf('___JSON_END___');
    if (jsonEndIdx !== -1) {
      const jsonContent = afterStart.substring(0, jsonEndIdx);
      const afterJson = afterStart.substring(jsonEndIdx + '___JSON_END___'.length);
      // afterJson 继续追加到 buffer
      if (afterJson) contentBuffer.value = afterJson;
      return { text: beforeJson, jsonBlock: jsonContent };
    } else {
      contentBuffer.value = afterStart;
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
    // ... 渲染 HTML 表格
  } catch {
    return '';
  }
};
```

#### 修改 SSE 事件处理

```typescript
es.onmessage = (evt) => {
  const chunk = evt.data ?? '';
  const { text, jsonBlock } = detectJsonBlock(chunk);
  const msg = chatMessages.value.find((m) => m.id === aiMsgId);

  if (msg) {
    if (msg.status === 'thinking') msg.status = 'speaking';

    // 文本直接追加
    if (text) {
      msg.text += text;
    }

    // JSON 块：解析并插入表格 HTML
    if (jsonBlock) {
      msg.text += parseTableJson(jsonBlock);
    }
  }
  scrollToBottom();
};
```

#### 修改渲染逻辑

```typescript
// renderMessage 保持不变，因为 JSON 块已经是 HTML 表格格式
// 但需要确保 :deep() 样式正确应用
```

### 3.2 样式增强

在现有 `.text` 样式基础上，确保表格样式正确：

```less
.text {
  :deep(.ai-table) {
    margin: 12px 0;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 1px 3px rgba(0,0,0,0.08);
    border: 1px solid #e8ecf0;
  }
  :deep(.ai-table-title) {
    background: linear-gradient(135deg, #4684e2 0%, #5a9aed 100%);
    color: #fff;
    font-weight: 600;
    padding: 10px 16px;
    font-size: 14px;
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
}
```

---

## 四、变更文件清单

| 文件 | 变更类型 |
|------|---------|
| `server/src/main/resources/Prompts/SystemPrompt.txt` | 新增输出格式规则章节 |
| `server/src/main/java/com/gk/study/ai/AiReactService.java` | `@SystemMessage` 改为 `fromResource` 加载 |
| `web/src/views/index/consultation.vue` | 新增 JSON 块解析逻辑，修改 SSE 处理，样式增强 |

---

## 五、验收标准

| 验收项 | 验证方法 |
|--------|---------|
| 非列表类问题正常显示文本 | 问"SQL入门经典多少钱"，观察文本输出 |
| 列表类问题显示为 HTML 表格 | 问"有哪些计算机类图书"，观察表格渲染 |
| 混合内容正确展示 | 问"推荐几个商品"，观察文本+表格混合 |
| 流式输出打字机效果正常 | 观察输出过程无闪烁、无断裂 |
| JSON 块正确解析 | 检查网络请求中 `___JSON_START___` 分隔符是否出现 |