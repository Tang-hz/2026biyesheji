# AI 客服表格渲染优化设计方案

## 1. 背景与目标

**问题**：AI 客服在回答商品列表类问题时，返回 markdown 格式的表格原始文本（如 `| 序号 | 书名 | ...|`），在聊天气泡中渲染效果不工整，视觉体验差。

**目标**：当 AI 需要展示列表/表格数据时，输出结构化 JSON；前端识别并渲染为 Ant Design 的 `<a-table>` 组件，呈现工整、专业的表格效果，与商城 admin 页面风格一致。

## 2. JSON 通信协议

### 表格类型消息

当 AI 需要展示列表或表格数据时，返回以下 JSON 结构（放在 SSE 输出的文本中，前端提取并单独渲染）：

```json
{
  "type": "table",
  "title": "精选计算机好书推荐",
  "columns": ["序号", "书名", "原价", "钻石会员价", "库存"],
  "rows": [
    ["1", "SQL入门经典（第5版）", "¥324.00", "¥275.40", "555本"],
    ["2", "TCP/IP入门经典（第5版）", "¥56.00", "¥47.60", "777本"]
  ]
}
```

- `type`: 固定为 `"table"`，标识该消息为结构化表格
- `title`: 可选，表格标题
- `columns`: 表头列名数组
- `rows`: 二维数组，每行一个数组

### 普通消息

非表格消息保持现有纯文本/ markdown 格式不变。

## 3. 前端渲染逻辑

### 消息解析流程

在 `consultation.vue` 的 `renderMarkdown` 函数中，按以下顺序检测：

```
1. 检查文本是否以 "{" 开头且可解析为 JSON
   → 如果解析成功且包含 "type": "table" → renderTable()
   → 否则继续 markdown 渲染
2. 检查是否包含 markdown 表格语法（| ... |）
   → 是 → 原有 marked 渲染
3. 其他 → 普通文本
```

### renderTable 实现

```typescript
const renderTable = (data: { title?: string; columns: string[]; rows: string[][] }) => {
  // 返回 a-table 所需的 columns + dataSource 结构
  // 序号列自动生成
};
```

渲染时在气泡内嵌入 `<a-table>`：
- `size="middle"` — 与 admin 页面一致
- `pagination=false` — 聊天内不分页
- `scroll="{ x: 'max-content' }"` — 列太多时横向滚动
- `bordered=true` — 显式边框
- 宽度 `100%` 自适应气泡宽度

### 气泡内布局

```
┌─────────────────────────────────────┐
│  [Avatar]  您好呀~...              │
│            ┌─────────────────────┐ │
│            │ 精选计算机好书推荐    │ │
│            │ ┌──┬──────┬──────┐   │ │
│            │ │序号│书名  │库存  │   │ │
│            │ ├───┼──────┼──────┤   │ │
│            │ │1  │SQL…  │555本 │   │ │
│            │ └───┴──────┴──────┘   │ │
│            └─────────────────────┘ │
│                         10:30      │
└─────────────────────────────────────┘
```

## 4. 后端配置

无需改动 `AiReactService` 代码逻辑，只需要在 AI 的 system prompt 中增加示例说明。

在 `AiModelConfiguration.java` 或 AI 配置处，为 AI 添加以下说明（作为 system prompt 的一部分）：

> 当你需要列举多个条目（如商品列表、订单列表、积分明细等）时，请使用以下 JSON 格式返回：
> ```json
> {"type":"table","title":"标题","columns":["列1","列2"],"rows":[["值1","值2"],["值3","值4"]]}
> ```
> 不要使用 markdown 表格格式。

## 5. 改动范围

| 文件 | 改动内容 |
|------|----------|
| `web/src/views/index/consultation.vue` | 新增 `renderTable()` 解析函数；修改 `renderMarkdown()` 增加 JSON 检测分支 |
| `server/src/main/java/com/gk/study/ai/config/AiModelConfiguration.java` | 在 `aiReactService` 构建处增加 system prompt 说明 |

## 6. 样式一致性

表格样式与 admin 页面保持一致：
- 字体：`#152844` 文字色，`14px` 字号
- 表头背景：`#e8eff7`
- 边框：`1px solid #ddd`
- 行高亮：`tr:hover { background: #f0f7ff }`
