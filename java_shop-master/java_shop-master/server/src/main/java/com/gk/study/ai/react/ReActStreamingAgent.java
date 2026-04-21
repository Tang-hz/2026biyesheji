package com.gk.study.ai.react;

import com.gk.study.ai.tool.AiMemberTool;
import com.gk.study.ai.tool.AiOrderRedeemTool;
import com.gk.study.ai.tool.AiOrderTool;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Function;

/**
 * Streaming ReAct Agent: merges ReAct loop control with SSE streaming output.
 *
 * Internal flow:
 * 1. LLM decides whether to call tools (ReAct reasoning)
 * 2. If tool call needed → execute → feed observation back
 * 3. Repeat until LLM produces final response
 * 4. Stream final response to user via SSE (Flux<String>)
 *
 * The thought/action/observation loop is internal only.
 * User only sees the final streamed response.
 */
@Component
public class ReActStreamingAgent {

    private static final Logger log = LoggerFactory.getLogger(ReActStreamingAgent.class);
    private static final int MAX_LOOPS = 5;

    private final ChatLanguageModel chatModel;
    private final Map<String, ToolInvoker> toolRegistry;

    public ReActStreamingAgent(
            ChatLanguageModel chatModel,
            AiOrderTool aiOrderTool,
            AiOrderRedeemTool aiOrderRedeemTool,
            AiMemberTool aiMemberTool) {

        // Use non-streaming ChatLanguageModel for ReAct loop control
        this.chatModel = chatModel;

        // Build tool registry
        this.toolRegistry = new HashMap<>();

        // AiOrderTool
        register("searchThingsByKeyword",
            (args) -> aiOrderTool.searchThingsByKeyword(argStr(args, "keyword", "")));
        register("getUserOrderByOrderNumber",
            (args) -> aiOrderTool.getUserOrderByOrderNumber(
                argStr(args, "orderNumber", ""),
                argStr(args, "userId", "")));
        register("orderByThingId",
            (args) -> aiOrderTool.orderByThingId(
                argStr(args, "thingId", ""),
                argInt(args, "count"),
                argStr(args, "remark", null),
                argInt(args, "redeemPoints"),
                argStr(args, "userId", "")));
        register("orderByThingTitle",
            (args) -> aiOrderTool.orderByThingTitle(
                argStr(args, "thingTitle", ""),
                argInt(args, "count"),
                argStr(args, "remark", null),
                argInt(args, "redeemPoints"),
                argStr(args, "userId", "")));

        // AiMemberTool
        register("getMemberInfo",
            (args) -> aiMemberTool.getMemberInfo(argStr(args, "userId", "")));
        register("getPointsInfo",
            (args) -> aiMemberTool.getPointsInfo(argStr(args, "userId", "")));
        register("calculatePurchaseBenefit",
            (args) -> aiMemberTool.calculatePurchaseBenefit(
                argDecimal(args, "price"),
                argInt(args, "count"),
                argStr(args, "userId", "")));

        // AiOrderRedeemTool
        register("placeOrderWithRedeem",
            (args) -> aiOrderRedeemTool.placeOrderWithRedeem(
                argStr(args, "thingId", ""),
                argInt(args, "count"),
                argInt(args, "redeemPoints"),
                argStr(args, "remark", null),
                argStr(args, "userId", "")));
    }

    private void register(String name, Function<Map<String, Object>, String> invoker) {
        toolRegistry.put(name, new ToolInvoker(invoker));
    }

    // ========== Argument helpers ==========

    private String argStr(Map<String, Object> args, String key, String def) {
        Object v = args.get(key);
        return v != null ? v.toString() : def;
    }

    private Integer argInt(Map<String, Object> args, String key) {
        Object v = args.get(key);
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).intValue();
        try { return Integer.parseInt(v.toString()); } catch (Exception e) { return null; }
    }

    private java.math.BigDecimal argDecimal(Map<String, Object> args, String key) {
        Object v = args.get(key);
        if (v == null) return null;
        if (v instanceof Number) return java.math.BigDecimal.valueOf(((Number) v).doubleValue());
        try { return new java.math.BigDecimal(v.toString()); } catch (Exception e) { return null; }
    }

    // ========== Main streaming entry point ==========

    /**
     * Stream final response after ReAct loop.
     * The ReAct loop (Thought→Action→Observation) runs internally,
     * only the final response is streamed to the user.
     */
    public Flux<String> chat(String userMessage, String memoryId) {
        String msg = userMessage == null ? "" : userMessage.trim();
        if (msg.isEmpty()) {
            return Flux.just("您好，请问有什么可以帮到您？");
        }

        String id = (memoryId == null || memoryId.isBlank()) ? "guest" : memoryId.trim();

        String systemPrompt = loadSystemPrompt();
        List<String> conversationHistory = new ArrayList<>();
        conversationHistory.add("User: " + msg);

        String finalResponse = runReActLoop(systemPrompt, conversationHistory, id);

        // Stream the final response token by token
        return streamTokens(finalResponse);
    }

    /**
     * Run the ReAct loop internally, return the final response.
     */
    private String runReActLoop(String systemPrompt, List<String> history, String userId) {
        for (int i = 0; i < MAX_LOOPS; i++) {
            log.debug("ReAct loop iteration {}", i + 1);

            String prompt = buildPrompt(systemPrompt, history);
            String llmResponse = chatModel.generate(prompt);

            if (llmResponse == null || llmResponse.isBlank()) {
                return "抱歉，我现在无法回答您的问题，请稍后重试。";
            }

            // Parse tool call from response
            ToolCallResult toolCall = parseToolCall(llmResponse);

            if (toolCall != null) {
                Map<String, Object> args = parseArguments(toolCall.arguments);
                if (!args.containsKey("userId") && needsUserId(toolCall.name)) {
                    args.put("userId", userId);
                }

                log.info("Executing tool: {} with args: {}", toolCall.name, args);
                String toolResult = executeTool(toolCall.name, args);

                history.add("Assistant: " + llmResponse);
                history.add("Observation: " + toolResult);
                log.debug("Tool result: {}", toolResult);
            } else {
                history.add("Assistant: " + llmResponse);
                return llmResponse;
            }
        }
        return "抱歉，我需要更多信息来回答您的问题。";
    }

    private boolean needsUserId(String toolName) {
        return !toolName.equals("searchThingsByKeyword");
    }

    private String buildPrompt(String systemPrompt, List<String> history) {
        StringBuilder sb = new StringBuilder();
        sb.append(systemPrompt).append("\n\n");
        sb.append("Conversation:\n");
        for (String h : history) {
            sb.append(h).append("\n");
        }
        return sb.toString();
    }

    // ========== Tool call parsing ==========

    private ToolCallResult parseToolCall(String response) {
        int jsonStart = response.indexOf("{");
        int jsonEnd = response.lastIndexOf("}");
        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            String json = response.substring(jsonStart, jsonEnd + 1);
            try {
                return parseToolCallJson(json);
            } catch (Exception e) {
                log.debug("JSON parse failed: {}", e.getMessage());
            }
        }
        // Check for tool names in text
        for (String name : toolRegistry.keySet()) {
            if (response.contains(name)) {
                return new ToolCallResult(name, extractArgs(response, name));
            }
        }
        return null;
    }

    private ToolCallResult parseToolCallJson(String json) {
        String name = extractJsonField(json, "name");
        if (name != null && !name.isEmpty()) {
            return new ToolCallResult(name, extractJsonField(json, "arguments"));
        }
        name = extractJsonField(json, "tool");
        if (name != null && !name.isEmpty()) {
            return new ToolCallResult(name, extractJsonField(json, "params"));
        }
        return null;
    }

    private String extractJsonField(String json, String field) {
        String search = "\"" + field + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) return "{}";
        int colon = json.indexOf(":", idx);
        if (colon < 0) return "{}";
        int valueStart = json.indexOf("\"", colon);
        if (valueStart < 0) return "{}";
        int valueEnd = json.indexOf("\"", valueStart + 1);
        if (valueEnd < 0) return "{}";
        return json.substring(valueStart + 1, valueEnd);
    }

    private String extractArgs(String response, String toolName) {
        int idx = response.indexOf(toolName);
        if (idx < 0) return "{}";
        String after = response.substring(idx);
        int start = after.indexOf("{");
        int end = after.lastIndexOf("}");
        if (start >= 0 && end > start) {
            return after.substring(start, end + 1);
        }
        return "{}";
    }

    private Map<String, Object> parseArguments(String argsJson) {
        Map<String, Object> args = new HashMap<>();
        if (argsJson == null || argsJson.isEmpty() || argsJson.equals("{}")) {
            return args;
        }
        try {
            String content = argsJson.trim();
            if (content.startsWith("{")) content = content.substring(1);
            if (content.endsWith("}")) content = content.substring(0, content.length() - 1);

            int i = 0;
            while (i < content.length()) {
                while (i < content.length() && (content.charAt(i) == ' ' || content.charAt(i) == ',')) i++;
                if (i >= content.length()) break;
                if (content.charAt(i) != '"') { i++; continue; }

                int keyStart = i + 1;
                int keyEnd = content.indexOf('"', keyStart);
                if (keyEnd < 0) break;
                String key = content.substring(keyStart, keyEnd);

                i = keyEnd + 1;
                while (i < content.length() && content.charAt(i) != ':') i++;
                i++;
                while (i < content.length() && content.charAt(i) == ' ') i++;

                if (i < content.length()) {
                    if (content.charAt(i) == '"') {
                        int vStart = i + 1;
                        int vEnd = content.indexOf('"', vStart);
                        if (vEnd > vStart) {
                            args.put(key, content.substring(vStart, vEnd));
                            i = vEnd + 1;
                        } else { i++; }
                    } else if (content.charAt(i) == '{' || content.charAt(i) == '[') {
                        i++;
                    } else {
                        int vEnd = i;
                        while (vEnd < content.length() && content.charAt(vEnd) != ',' && content.charAt(vEnd) != '}') vEnd++;
                        String val = content.substring(i, vEnd).trim();
                        if (val.matches("-?\\d+")) args.put(key, Integer.parseInt(val));
                        else if (val.matches("-?\\d+\\.\\d+")) args.put(key, new java.math.BigDecimal(val));
                        i = vEnd;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse args: {}", argsJson, e);
        }
        return args;
    }

    private String executeTool(String name, Map<String, Object> args) {
        ToolInvoker invoker = toolRegistry.get(name);
        if (invoker == null) return "Tool not found: " + name;
        try {
            Object result = invoker.apply(args);
            return result != null ? result.toString() : "Tool returned null";
        } catch (Exception e) {
            log.error("Tool execution error: {}", e.getMessage(), e);
            return "工具执行出错: " + e.getMessage();
        }
    }

    // ========== Streaming helpers ==========

    private Flux<String> streamTokens(String text) {
        if (text == null || text.isEmpty()) {
            return Flux.empty();
        }
        // Stream as a single chunk for simplicity
        // In production, could tokenize and stream word by word
        return Flux.just(text);
    }

    private String loadSystemPrompt() {
        try {
            var stream = getClass().getClassLoader().getResourceAsStream("Prompts/SystemPrompt.txt");
            if (stream != null) return new String(stream.readAllBytes());
        } catch (Exception e) {
            log.warn("Failed to load system prompt", e);
        }
        return "You are a helpful AI customer service assistant.";
    }

    // ========== Internal classes ==========

    private record ToolCallResult(String name, String arguments) {}

    private static class ToolInvoker {
        private final Function<Map<String, Object>, String> apply;

        ToolInvoker(Function<Map<String, Object>, String> apply) {
            this.apply = apply;
        }

        String apply(Map<String, Object> args) {
            return apply.apply(args);
        }
    }
}