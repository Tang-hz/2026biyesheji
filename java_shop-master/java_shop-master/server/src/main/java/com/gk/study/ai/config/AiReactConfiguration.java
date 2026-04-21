package com.gk.study.ai.config;

import com.gk.study.ai.react.ReActAgent;
import com.gk.study.ai.tool.AiMemberTool;
import com.gk.study.ai.tool.AiOrderRedeemTool;
import com.gk.study.ai.tool.AiOrderTool;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for ReAct AI components.
 * Uses ChatLanguageModel directly for manual loop control (not @AiService).
 */
@Configuration
public class AiReactConfiguration {

    @Bean
    public ReActAgent reActAgent(
            ChatLanguageModel chatLanguageModel,
            AiOrderTool aiOrderTool,
            AiOrderRedeemTool aiOrderRedeemTool,
            AiMemberTool aiMemberTool) {
        return new ReActAgent(chatLanguageModel, aiOrderTool, aiOrderRedeemTool, aiMemberTool);
    }
}