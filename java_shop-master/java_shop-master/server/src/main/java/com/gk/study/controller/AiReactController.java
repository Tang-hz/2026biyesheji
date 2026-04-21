package com.gk.study.controller;

import com.gk.study.ai.AiReactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * ReAct AI 客服端点：声明式 @AiService，LangChain4j 框架自动跑 ReAct 循环 + SSE 流式输出。
 *
 * GET /api/ai/react/stream?message=问题&userId=用户ID
 */
@Tag(name = "ReAct AI客服控制层")
@RestController
@RequestMapping("/ai/react")
public class AiReactController {

    private final AiReactService aiReactService;

    public AiReactController(AiReactService aiReactService) {
        this.aiReactService = aiReactService;
    }

    @Operation(summary = "ReAct客服对话（SSE流式，@AiService声明式，框架自动ReAct循环）")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(
            @RequestParam("message") String message,
            @RequestParam(value = "userId", required = false) String userId) {

        String userMessage = (message == null) ? "" : message.trim();
        if (userMessage.isEmpty()) {
            return Flux.just("请输入问题");
        }

        String memoryId = resolveMemoryId(userId);
        return aiReactService.chat(memoryId, userMessage);
    }

    private String resolveMemoryId(String userId) {
        if (userId == null || userId.isBlank()) {
            return "guest";
        }
        return userId.trim();
    }
}