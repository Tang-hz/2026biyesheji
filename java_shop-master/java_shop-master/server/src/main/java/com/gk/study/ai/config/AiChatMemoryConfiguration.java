package com.gk.study.ai.config;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * LangChain4j：{@link ChatMemoryStore}（由 {@link com.gk.study.config.RedisConfig} 提供 Redis 实现）
 * + {@link MessageWindowChatMemory} + {@link ChatMemoryProvider}，按 {@code memoryId} 隔离会话。
 */
@Configuration
@EnableConfigurationProperties(AiCustomerMemoryProperties.class)
public class AiChatMemoryConfiguration {

    @Bean
    public ChatMemoryProvider chatMemoryProvider(
            ChatMemoryStore chatMemoryStore,
            AiCustomerMemoryProperties properties) {
        int max = Math.max(2, properties.getMaxMessages());
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(max)
                .chatMemoryStore(chatMemoryStore)
                .build();
    }
}
