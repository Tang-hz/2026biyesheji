package com.gk.study.ai.config;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * LangChain4j 标准组合：{@link InMemoryChatMemoryStore} + {@link MessageWindowChatMemory}
 * + {@link ChatMemoryProvider}，按 {@code memoryId}（通常为登录用户 ID）隔离会话。
 * <p>
 * 生产环境可将 {@link InMemoryChatMemoryStore} 换为持久化 {@link dev.langchain4j.store.memory.chat.ChatMemoryStore}
 * 实现，{@link ChatMemoryProvider} 的装配方式不变。
 */
@Configuration
@EnableConfigurationProperties(AiCustomerMemoryProperties.class)
public class AiChatMemoryConfiguration {

    @Bean
    public InMemoryChatMemoryStore chatMemoryStore() {
        return new InMemoryChatMemoryStore();
    }

    @Bean
    public ChatMemoryProvider chatMemoryProvider(
            InMemoryChatMemoryStore chatMemoryStore,
            AiCustomerMemoryProperties properties) {
        int max = Math.max(2, properties.getMaxMessages());
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(max)
                .chatMemoryStore(chatMemoryStore)
                .build();
    }
}
