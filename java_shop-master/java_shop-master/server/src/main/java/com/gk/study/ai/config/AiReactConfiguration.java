package com.gk.study.ai.config;

import com.gk.study.ai.react.ReActAiService;
import com.gk.study.ai.tool.AiMemberTool;
import com.gk.study.ai.tool.AiOrderRedeemTool;
import com.gk.study.ai.tool.AiOrderTool;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for ReAct AI components.
 * Reuses the same ChatLanguageModel and tools as RagAnswerAi,
 * but exposes a separate ReActAiService that enables tool-calling autonomy.
 */
@Configuration
public class AiReactConfiguration {

    @Bean
    public ReActAiService reActAiService(
            StreamingChatLanguageModel streamingChatLanguageModel,
            AiOrderTool aiOrderTool,
            AiOrderRedeemTool aiOrderRedeemTool,
            AiMemberTool aiMemberTool) {
        return AiServices.builder(ReActAiService.class)
                .streamingChatLanguageModel(streamingChatLanguageModel)
                .tools(aiOrderTool, aiOrderRedeemTool, aiMemberTool)
                .build();
    }
}
