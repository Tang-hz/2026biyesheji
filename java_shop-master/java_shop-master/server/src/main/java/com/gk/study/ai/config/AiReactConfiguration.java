package com.gk.study.ai.config;

import com.gk.study.ai.tool.AiMemberTool;
import com.gk.study.ai.tool.AiOrderRedeemTool;
import com.gk.study.ai.tool.AiOrderTool;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * Configuration for ReAct AI components.
 * ReActAgent is auto-scanned via @Component, this config provides
 * additional beans if needed.
 */
@Configuration
public class AiReactConfiguration {

    // ReActAgent is already a @Component, no need for @Bean here
}