package com.gk.study.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AI 客服对话记忆（LangChain4j {@link dev.langchain4j.memory.chat.MessageWindowChatMemory}）。
 * <p>
 * {@code maxMessages}：窗口内保留的最大消息条数（每条为用户一条或助手一条）。
 * 15 轮问答 ≈ 15 条用户消息 + 15 条助手消息 = 30 条。
 */
@ConfigurationProperties(prefix = "ai.customer.memory")
public class AiCustomerMemoryProperties {

    /**
     * 记忆窗口最大消息数（默认 30 = 15 轮）。
     */
    private int maxMessages = 30;

    public int getMaxMessages() {
        return maxMessages;
    }

    public void setMaxMessages(int maxMessages) {
        this.maxMessages = maxMessages;
    }
}
