package com.gk.study.ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import reactor.core.publisher.Flux;

/**
 * 声明式 ReAct AI 客服：LangChain4j @AiService 框架自动跑 ReAct 循环 + SSE 流式输出。
 *
 * 特性（由 AiModelConfiguration.ragAnswerAi 配置决定）：
 * - 每轮自动 RAG 检索知识库（contentRetriever）
 * - 工具调用（AiOrderTool / AiMemberTool / AiOrderRedeemTool）
 * - ChatMemory 对话记忆
 * - Streaming SSE 流式输出
 */
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

    /**
     * ReAct 客服对话（SSE 流式，框架自动跑 ReAct 循环）
     *
     * @param memoryId 会话 ID（对应 ChatMemory）
     * @param userMessage 用户消息
     * @return SSE 流式响应
     */
    Flux<String> chat(@MemoryId String memoryId, @UserMessage String userMessage);
}
