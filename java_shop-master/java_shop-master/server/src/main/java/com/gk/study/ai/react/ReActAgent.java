package com.gk.study.ai.react;

import com.gk.study.ai.tool.AiMemberTool;
import com.gk.study.ai.tool.AiOrderRedeemTool;
import com.gk.study.ai.tool.AiOrderTool;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Function;

/**
 * ReAct Agent: encapsulates the reasoning-act loop with full control.
 *
 * Flow:
 * 1. LLM decides whether to call a tool based on user message + conversation history
 * 2. If tool call needed → parse tool name + args → execute via reflection → get result
 * 3. Feed observation back to LLM → continue reasoning
 * 4. Repeat until LLM produces final response (no more tool calls)
 *
 * Max loops = 5 to prevent infinite loops.
 * SystemPrompt.txt rules still apply (no Thought/Action tags visible to user).
 */
@Component
public class ReActAgent {

    private static final Logger log = LoggerFactory.getLogger(ReActAgent.class);
    private static final int MAX_LOOPS = 5;

    private final ChatLanguageModel chatModel;
    private final Map<String, ToolMethod> toolRegistry;

    public ReActAgent(
            ChatLanguageModel chatLanguageModel,
            AiOrderTool aiOrderTool,
            AiOrderRedeemTool aiOrderRedeemTool,
            AiMemberTool aiMemberTool) {

        this.chatModel = chatLanguageModel;

        // Build tool registry manually - map tool names to method references
        this.toolRegistry = new HashMap<>();

        // AiOrderTool methods
        registerToolMethod(aiOrderTool, "searchThingsByKeyword",
            (args) -> aiOrderTool.searchThingsByKeyword((String) args));
        registerToolMethod(aiOrderTool, "getUserOrderByOrderNumber",
            (args) -> aiOrderTool.getUserOrderByOrderNumber(
                (String) ((Map<?, ?>) args).get("orderNumber"),
                (String) ((Map<?, ?>) args).get("userId")));
        registerToolMethod(aiOrderTool, "orderByThingId",
            (args) -> aiOrderTool.orderByThingId(
                (String) ((Map<?, ?>) args).get("thingId"),
                (Integer) ((Map<?, ?>) args).get("count"),
                (String) ((Map<?, ?>) args).get("remark"),
                (Integer) ((Map<?, ?>) args).get("redeemPoints"),
                (String) ((Map<?, ?>) args).get("userId")));
        registerToolMethod(aiOrderTool, "orderByThingTitle",
            (args) -> aiOrderTool.orderByThingTitle(
                (String) ((Map<?, ?>) args).get("thingTitle"),
                (Integer) ((Map<?, ?>) args).get("count"),
                (String) ((Map<?, ?>) args).get("remark"),
                (Integer) ((Map<?, ?>) args).get("redeemPoints"),
                (String) ((Map<?, ?>) args).get("userId")));

        // AiMemberTool methods
        registerToolMethod(aiMemberTool, "getMemberInfo",
            (args) -> aiMemberTool.getMemberInfo((String) args));
        registerToolMethod(aiMemberTool, "getPointsInfo",
            (args) -> aiMemberTool.getPointsInfo((String) args));
        registerToolMethod(aiMemberTool, "calculatePurchaseBenefit",
            (args) -> aiMemberTool.calculatePurchaseBenefit(
                (java.math.BigDecimal) ((Map<?, ?>) args).get("price"),
                (Integer) ((Map<?, ?>) args).get("count"),
                (String) ((Map<?, ?>) args).get("userId")));

        // AiOrderRedeemTool methods
        registerToolMethod(aiOrderRedeemTool, "placeOrderWithRedeem",
            (args) -> aiOrderRedeemTool.placeOrderWithRedeem(
                (String) ((Map<?, ?>) args).get("thingId"),
                (Integer) ((Map<?, ?>) args).get("count"),
                (Integer) ((Map<?, ?>) args).get("redeemPoints"),
                (String) ((Map<?, ?>) args).get("remark"),
                (String) ((Map<?, ?>) args).get("userId")));
    }

    private void registerToolMethod(Object tool, String toolName, Function<Object, String> invoker) {
        toolRegistry.put(toolName, new ToolMethod(tool, invoker));
    }

    /**
     * Single-shot chat: runs ReAct loop internally, returns final response.
     */
    public String chat(String userMessage, String memoryId) {
        String msg = (userMessage == null) ? "" : userMessage.trim();
        if (msg.isEmpty()) {
            return "您好，请问有什么可以帮到您？";
        }

        String id = (memoryId == null || memoryId.isBlank()) ? "guest" : memoryId.trim();

        List<Interaction> history = new ArrayList<>();
        history.add(new Interaction("user", msg));

        String systemPrompt = loadSystemPrompt();
        String conversationHistory = "";

        for (int i = 0; i < MAX_LOOPS; i++) {
            log.debug("ReAct loop iteration {}", i + 1);

            String prompt = buildPrompt(systemPrompt, conversationHistory, history);
            String llmResponse = chatModel.generate(prompt);

            if (llmResponse == null || llmResponse.isBlank()) {
                return "抱歉，我现在无法回答您的问题，请稍后重试。";
            }

            log.debug("LLM response: {}", llmResponse);

            // Parse tool call from response
            ToolCall toolCall = parseToolCall(llmResponse);

            if (toolCall != null) {
                // Inject userId into args for tools that need it
                Map<String, Object> args = parseArguments(toolCall.arguments);
                if (!args.containsKey("userId") && needsUserId(toolCall.name)) {
                    args.put("userId", id);
                }

                log.info("Executing tool: {} with args: {}", toolCall.name, args);
                String toolResult;
                try {
                    toolResult = executeTool(toolCall.name, args);
                } catch (Exception e) {
                    toolResult = "工具执行失败: " + e.getMessage();
                    log.error("Tool execution error", e);
                }

                history.add(new Interaction("assistant", llmResponse));
                history.add(new Interaction("tool", toolResult));
                conversationHistory += "\nAssistant: " + llmResponse + "\nObservation: " + toolResult;

                log.debug("Tool result: {}", toolResult);
            } else {
                // No tool call → final response
                history.add(new Interaction("assistant", llmResponse));
                return llmResponse;
            }
        }

        return "抱歉，我需要更多信息来回答您的问题。";
    }

    private boolean needsUserId(String toolName) {
        return !toolName.equals("searchThingsByKeyword");
    }

    private String buildPrompt(String systemPrompt, String conversationHistory, List<Interaction> history) {
        StringBuilder sb = new StringBuilder();
        sb.append(systemPrompt).append("\n\n");

        if (!conversationHistory.isEmpty()) {
            sb.append("Conversation history:").append(conversationHistory).append("\n\n");
        }

        sb.append("Current conversation:\n");
        for (Interaction i : history) {
            switch (i.role) {
                case "user" -> sb.append("User: ").append(i.content).append("\n");
                case "assistant" -> sb.append("Assistant: ").append(i.content).append("\n");
                case "tool" -> sb.append("Observation: ").append(i.content).append("\n");
            }
        }
        return sb.toString();
    }

    private ToolCall parseToolCall(String response) {
        // Try to find JSON object in response
        int jsonStart = response.indexOf("{");
        int jsonEnd = response.lastIndexOf("}");

        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            String jsonStr = response.substring(jsonStart, jsonEnd + 1);
            try {
                return parseToolCallJson(jsonStr);
            } catch (Exception e) {
                log.debug("Failed to parse tool call JSON: {}", e.getMessage());
            }
        }

        // Check for tool names in response
        for (String toolName : toolRegistry.keySet()) {
            if (response.contains(toolName)) {
                String args = extractArguments(response, toolName);
                return new ToolCall(toolName, args);
            }
        }

        return null;
    }

    private ToolCall parseToolCallJson(String json) {
        String name = extractJsonField(json, "name");
        String args = extractJsonField(json, "arguments");
        if (name != null && !name.isEmpty()) {
            return new ToolCall(name, args != null ? args : "{}");
        }
        // Try alternative field names
        name = extractJsonField(json, "tool");
        if (name != null && !name.isEmpty()) {
            return new ToolCall(name, extractJsonField(json, "params"));
        }
        return null;
    }

    private String extractJsonField(String json, String field) {
        String search = "\"" + field + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) return null;
        int colonIdx = json.indexOf(":", idx);
        if (colonIdx < 0) return null;
        int valueStart = json.indexOf("\"", colonIdx);
        if (valueStart < 0) return null;
        int valueEnd = json.indexOf("\"", valueStart + 1);
        if (valueEnd < 0) return null;
        return json.substring(valueStart + 1, valueEnd);
    }

    private String extractArguments(String response, String toolName) {
        int toolIdx = response.indexOf(toolName);
        if (toolIdx < 0) return "{}";
        String after = response.substring(toolIdx);
        int argsStart = after.indexOf("{");
        int argsEnd = after.lastIndexOf("}");
        if (argsStart >= 0 && argsEnd > argsStart) {
            return after.substring(argsStart, argsEnd + 1);
        }
        return "{}";
    }

    private Map<String, Object> parseArguments(String argsJson) {
        Map<String, Object> args = new HashMap<>();
        if (argsJson == null || argsJson.isEmpty() || argsJson.equals("{}")) {
            return args;
        }
        // Simple JSON parsing for argument extraction
        // Handles: {"key": "value", "key2": 123}
        try {
            // Remove braces
            String content = argsJson.trim();
            if (content.startsWith("{")) content = content.substring(1);
            if (content.endsWith("}")) content = content.substring(0, content.length() - 1);

            // Parse key-value pairs
            int i = 0;
            while (i < content.length()) {
                // Skip whitespace and commas
                while (i < content.length() && (content.charAt(i) == ' ' || content.charAt(i) == ',')) i++;
                if (i >= content.length()) break;

                // Find key
                if (content.charAt(i) != '"') {
                    i++;
                    continue;
                }
                int keyStart = i + 1;
                int keyEnd = content.indexOf('"', keyStart);
                if (keyEnd < 0) break;
                String key = content.substring(keyStart, keyEnd);

                // Skip to colon
                i = keyEnd + 1;
                while (i < content.length() && content.charAt(i) != ':') i++;
                i++; // skip colon

                // Skip whitespace
                while (i < content.length() && content.charAt(i) == ' ') i++;

                // Parse value
                if (i < content.length()) {
                    if (content.charAt(i) == '"') {
                        int valueStart = i + 1;
                        int valueEnd = content.indexOf('"', valueStart);
                        if (valueEnd > valueStart) {
                            args.put(key, content.substring(valueStart, valueEnd));
                            i = valueEnd + 1;
                        } else {
                            i++;
                        }
                    } else if (content.charAt(i) == '{' || content.charAt(i) == '[') {
                        // Nested object/array - skip for simplicity
                        i++;
                    } else {
                        // Number or boolean
                        int valueEnd = i;
                        while (valueEnd < content.length() &&
                               content.charAt(valueEnd) != ',' &&
                               content.charAt(valueEnd) != '}') {
                            valueEnd++;
                        }
                        String value = content.substring(i, valueEnd).trim();
                        if (value.matches("-?\\d+")) {
                            args.put(key, Integer.parseInt(value));
                        } else if (value.matches("-?\\d+\\.\\d+")) {
                            args.put(key, new java.math.BigDecimal(value));
                        }
                        i = valueEnd;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse arguments JSON: {}", argsJson, e);
        }
        return args;
    }

    private String executeTool(String toolName, Map<String, Object> args) {
        ToolMethod tool = toolRegistry.get(toolName);
        if (tool == null) {
            return "Tool not found: " + toolName;
        }
        try {
            Object result = tool.invoker.apply(args);
            return result != null ? result.toString() : "Tool returned null";
        } catch (Exception e) {
            log.error("Tool execution failed for {}: {}", toolName, e.getMessage(), e);
            return "工具执行出错: " + e.getMessage();
        }
    }

    private String loadSystemPrompt() {
        try {
            var stream = getClass().getClassLoader().getResourceAsStream("Prompts/SystemPrompt.txt");
            if (stream != null) {
                return new String(stream.readAllBytes());
            }
        } catch (Exception e) {
            log.warn("Failed to load system prompt", e);
        }
        return "You are a helpful AI customer service assistant.";
    }

    private static class Interaction {
        final String role;
        final String content;
        Interaction(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    private static class ToolCall {
        final String name;
        final String arguments;
        ToolCall(String name, String arguments) {
            this.name = name;
            this.arguments = arguments;
        }
    }

    private static class ToolMethod {
        final Object tool;
        final Function<Object, String> invoker;
        ToolMethod(Object tool, Function<Object, String> invoker) {
            this.tool = tool;
            this.invoker = invoker;
        }
    }
}