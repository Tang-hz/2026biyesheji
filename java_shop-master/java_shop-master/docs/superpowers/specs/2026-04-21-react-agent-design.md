# ReAct Agent 设计文档

## 背景与目标

**问题**：当前 `RagAnswerAi` 使用 LangChain4j 的 `@AiService` 自动调用工具，但工具调用逻辑由框架预设，用户无法自主决定调用多个工具形成推理链。

**目标**：引入完整的 ReAct（Reasoning + Acting）范式，让 LLM 自主决定调用哪些工具、循环多少次，最终返回综合回复。

---

## 方案概述

新建 `ReActAgent` 类封装推理循环，复用现有 3 个 Tool，保持 SystemPrompt 不变。输出为一次性返回（不流式）。

---

## 技术设计

### 核心流程

```
用户消息
    ↓
ReActAgent.chat()
    ↓
┌─ Loop (LLM 自主决定循环次数) ─┐
│  1. Think: LLM 推理是否调用工具  │
│  2. Act:  如需调用 → 执行 Tool   │
│  3. Observe: 将结果注回 LLM      │
│  4. 判断是否达到终态             │
└──────────────────────────────┘
    ↓
Final Response → 返回给用户
```

### 组件列表

| 组件 | 路径 | 职责 |
|------|------|------|
| `ReActAgent` | `ai/react/ReActAgent.java` | 核心推理循环封装 |
| `ReActAiService` | `ai/react/ReActAiService.java` | AI Service 接口（LangChain4j） |
| `AiController` | `controller/AiController.java` | 添加 `/api/ai/react/chat` 端点 |

### 工具集（不变）

| Tool | 描述 |
|------|------|
| `searchThingsByKeyword` | 商品搜索 |
| `getUserOrderByOrderNumber` | 订单查询 |
| `orderByThingId` / `orderByThingTitle` | 按 ID/标题下单 |
| `placeOrderWithRedeem` | 积分抵扣下单 |
| `getMemberInfo` / `getPointsInfo` | 会员/积分查询 |
| `calculatePurchaseBenefit` | 购买优惠计算 |

---

## 实现细节

### ReActAgent 伪代码

```java
public class ReActAgent {
    private final ChatModel chatModel;
    private final ToolRegistry toolRegistry;
    private final SystemPrompt systemPrompt;

    public String chat(String userMessage, String memoryId) {
        String context = buildContext(userMessage, memoryId);
        List<ToolExecution> executions = new ArrayList<>();

        // Loop (由 LLM 决定终止)
        for (int i = 0; i < MAX_LOOPS; i++) {
            String llmResponse = chatModel.send(context + buildExecutionsSummary(executions));

            // 解析 LLM 响应：是否包含工具调用？
            if (hasToolCall(llmResponse)) {
                ToolResult result = executeTool(llmResponse);
                executions.add(new ToolExecution(llmResponse, result));
                // 将结果注回上下文继续推理
            } else {
                // 无工具调用，认为是 Final Response
                return extractFinalResponse(llmResponse);
            }
        }
        return "抱歉，我需要更多信息来回答您的问题。";
    }
}
```

### 工具调用解析

LLM 响应中解析工具调用的方式（Tool Calls 格式）：
- LangChain4j 内置支持 tool calling，结果以 `toolCalls` 字段返回
- 解析方式：检测响应是否包含 `toolCalls` → 提取 tool name + arguments → 执行

### 新增 Endpoint

```
GET /api/ai/react/chat?message={问题}&userId={用户ID}
Response:一次性返回最终回复
```

---

## 改动清单

| 文件 | 操作 |
|------|------|
| `server/src/main/java/com/gk/study/ai/react/ReActAgent.java` | **新建** |
| `server/src/main/java/com/gk/study/ai/react/ReActAiService.java` | **新建** |
| `server/src/main/java/com/gk/study/ai/config/AiReactConfiguration.java` | **新建**（配置 Bean） |
| `server/src/main/java/com/gk/study/controller/AiController.java` | **修改**（添加 endpoint） |

---

## 验证方式

1. 启动后端，调用 `/api/ai/react/chat?message=我想查一下我的订单&userId=1`
2. 观察控制台日志是否打印推理循环（可选调试模式）
3. 验证返回结果是否综合了工具调用结果（如同时查了商品+会员信息）
4. 确保现有 `/api/ai/customer-service/rag/stream` 端点不受影响

---

## 约束

- MAX_LOOPS = 5（防止无限循环）
- 现有 SystemPrompt.txt 保持不变（禁止出现 Thought/Action 等标签的规则仍然有效）
- 不改动现有数据库结构