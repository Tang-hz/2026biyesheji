package com.gk.study.ai.react;

import com.gk.study.ai.tool.AiMemberTool;
import com.gk.study.ai.tool.AiOrderRedeemTool;
import com.gk.study.ai.tool.AiOrderTool;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import reactor.core.publisher.Flux;

/**
 * ReAct AI Service: uses LangChain4j tool calling so the LLM can decide
 * which tool to invoke autonomously.
 */
@AiService
public interface ReActAiService {

    @SystemMessage(fromResource = "Prompts/SystemPrompt.txt")
    Flux<String> chat(@MemoryId String memoryId, @UserMessage String userMessage);
}
