package com.gk.study.ai;

import dev.langchain4j.service.MemoryId;
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
