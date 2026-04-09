package com.gk.study.controller;

import com.gk.study.ai.CustomerServiceAi;
import com.gk.study.service.RagCustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Tag(name = "AI客服控制层")
@RestController
@RequestMapping("/ai/customer-service")
public class AiCustomerServiceController {
    // NOTE:
    // 这里原先使用 flux.timeout(...) 做“空闲超时”会在模型/工具调用阶段长时间无输出时
    // 主动截断 SSE，前端表现为“连接断了”。
    // 由于你的需求是避免超时截断，所以不再启用此类空闲截断逻辑。

    private final CustomerServiceAi customerServiceAi;
    private final RagCustomerService ragCustomerService;

    public AiCustomerServiceController(CustomerServiceAi customerServiceAi,
                                       RagCustomerService ragCustomerService) {
        this.customerServiceAi = customerServiceAi;
        this.ragCustomerService = ragCustomerService;
    }

    /**
     * SSE 流式客服（多轮对话记忆，按 {@code userId} 隔离；未传时为 guest）
     *
     * 端点：/api/ai/customer-service/stream?message=&amp;userId=
     */
    @Operation(summary = "客服对话 SSE 流式（多轮记忆）")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(
            @RequestParam("message") String message,
            @RequestParam(value = "userId", required = false) String userId) {
        String userMessage = message == null ? "" : message.trim();
        if (userMessage.isEmpty()) {
            return Flux.just("请输入问题（例如：怎么申请退款？）");
        }
        String memoryId = resolveMemoryId(userId);

        return withSseTimeout(
                customerServiceAi.chat(memoryId, userMessage),
                "\n（本次响应超时，请稍后重试或简化问题。）");
    }

    /**
     * RAG + SSE 流式（多轮记忆，按 {@code userId} 隔离；未传则无记忆，与旧行为一致）
     *
     * 端点：/api/ai/customer-service/rag/stream?message=&amp;userId=
     */
    @Operation(summary = "RAG 客服 SSE 流式（知识库 + 多轮记忆）")
    @GetMapping(value = "/rag/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> ragStream(
            @RequestParam("message") String message,
            @RequestParam(value = "userId", required = false) String userId) {
        String userMessage = message == null ? "" : message.trim();
        if (userMessage.isEmpty()) {
            return Flux.just("请输入问题（例如：退款规则是什么？）");
        }
        String memoryId = resolveMemoryId(userId);

        return withSseTimeout(
                ragCustomerService.chatWithRag(userMessage, memoryId),
                "\n（本次RAG响应超时，请稍后重试或简化问题。）");
    }

    /**
     * 空或未登录时使用 {@code guest} 共享会话；登录用户请传真实用户 ID 以实现隔离。
     */
    private static String resolveMemoryId(String userId) {
        if (userId == null || userId.isBlank()) {
            return "guest";
        }
        return userId.trim();
    }

    private Flux<String> withSseTimeout(Flux<String> flux, String timeoutMessage) {
        return flux;
    }
}
