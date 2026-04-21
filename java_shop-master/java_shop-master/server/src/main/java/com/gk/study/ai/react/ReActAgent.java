package com.gk.study.ai.react;

import dev.langchain4j.model.chat.ChatLanguageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * ReAct Agent: encapsulates the reasoning-act loop.
 *
 * Flow:
 * 1. LLM decides whether to call a tool based on user message + conversation history
 * 2. If tool call needed → execute → feed observation back to LLM
 * 3. Repeat until LLM produces final response (no more tool calls)
 *
 * Max loops = 5 to prevent infinite loops.
 */
@Component
public class ReActAgent {

    private static final Logger log = LoggerFactory.getLogger(ReActAgent.class);
    private static final int MAX_LOOPS = 5;

    private final ReActAiService reActAiService;

    public ReActAgent(ReActAiService reActAiService) {
        this.reActAiService = reActAiService;
    }

    /**
     * Single-shot chat: runs ReAct loop internally, returns final response.
     *
     * @param userMessage the user question
     * @param memoryId    session identifier (userId or "guest")
     * @return final text response from LLM
     */
    public String chat(String userMessage, String memoryId) {
        String msg = (userMessage == null) ? "" : userMessage.trim();
        if (msg.isEmpty()) {
            return "您好，请问有什么可以帮到您？";
        }

        String id = (memoryId == null || memoryId.isBlank()) ? "guest" : memoryId.trim();

        List<Interaction> history = new ArrayList<>();
        history.add(new Interaction("user", msg));

        String lastAssistantMessage = null;

        for (int i = 0; i < MAX_LOOPS; i++) {
            log.debug("ReAct loop iteration {}", i + 1);

            String context = buildContext(history, lastAssistantMessage);

            // Call LLM - blockFirst() gets the first emitted item from Flux
            String response = reActAiService.chat(id, context).blockFirst();

            if (response == null || response.isBlank()) {
                return "抱歉，我现在无法回答您的问题，请稍后重试。";
            }

            lastAssistantMessage = response;

            // Check if response contains tool calls
            if (containsToolCalls(response)) {
                history.add(new Interaction("assistant", response));
                // For now, LangChain4j handles tool execution automatically in the streaming response
                // The response we get back after tool execution is the final textual response
                // So if we detect tool calls in response, we continue the loop with the result
                history.add(new Interaction("tool", "Tool execution completed"));
                log.debug("Tool call detected, continuing loop");
            } else {
                history.add(new Interaction("assistant", response));
                return response;
            }
        }

        return "抱歉，我需要更多信息来回答您的问题。";
    }

    private String buildContext(List<Interaction> history, String lastAssistant) {
        StringBuilder sb = new StringBuilder();
        for (Interaction i : history) {
            switch (i.role) {
                case "user" -> sb.append("User: ").append(i.content).append("\n");
                case "assistant" -> sb.append("Assistant: ").append(i.content).append("\n");
                case "tool" -> sb.append("Tool Result: ").append(i.content).append("\n");
            }
        }
        return sb.toString();
    }

    private boolean containsToolCalls(String response) {
        if (response == null) return false;
        String lower = response.toLowerCase();
        return response.contains("toolCalls") ||
               response.contains("tool_call") ||
               response.contains("function_call") ||
               (response.startsWith("[") && response.contains("invoking"));
    }

    private static class Interaction {
        final String role;
        final String content;

        Interaction(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}
