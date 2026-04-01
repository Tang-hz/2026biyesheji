package com.gk.study.ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import reactor.core.publisher.Flux;

/**
 * RAG 客服：{@link UserMessage} 仅为用户原话，由 LangChain4j 经 {@link dev.langchain4j.rag.content.retriever.ContentRetriever}
 * 每轮检索知识并注入提示；{@link dev.langchain4j.memory.ChatMemory} 中只累积用户原话与助手回复。
 */
@AiService
public interface RagAnswerAi {

    @SystemMessage(fromResource = "Prompts/SystemPrompt.txt")
    Flux<String> chat(@MemoryId String memoryId, @UserMessage String userMessage);
}
