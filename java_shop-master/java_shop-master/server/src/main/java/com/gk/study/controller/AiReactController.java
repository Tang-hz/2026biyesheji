package com.gk.study.controller;

import com.gk.study.ai.react.ReActAgent;
import com.gk.study.common.APIResponse;
import com.gk.study.common.ResponeCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * ReAct Agent REST endpoint.
 * Provides a non-streaming, one-shot chat interface where the LLM
 * autonomously decides tool calls within the ReAct reasoning loop.
 */
@Tag(name = "ReAct AI客服控制层")
@RestController
@RequestMapping("/ai/react")
public class AiReactController {

    private final ReActAgent reActAgent;

    public AiReactController(ReActAgent reActAgent) {
        this.reActAgent = reActAgent;
    }

    /**
     * One-shot chat with ReAct reasoning loop.
     *
     * GET /api/ai/react/chat?message=我想查一下我的订单&userId=1
     */
    @Operation(summary = "ReAct客服对话（一次性返回）")
    @GetMapping("/chat")
    public APIResponse<String> chat(
            @RequestParam("message") String message,
            @RequestParam(value = "userId", required = false) String userId) {

        String userMessage = (message == null) ? "" : message.trim();
        if (userMessage.isEmpty()) {
            return new APIResponse<>(ResponeCode.FAIL, "请输入问题");
        }

        String memoryId = resolveMemoryId(userId);
        String response = reActAgent.chat(userMessage, memoryId);

        return new APIResponse<>(ResponeCode.SUCCESS, "success", response);
    }

    private String resolveMemoryId(String userId) {
        if (userId == null || userId.isBlank()) {
            return "guest";
        }
        return userId.trim();
    }
}
