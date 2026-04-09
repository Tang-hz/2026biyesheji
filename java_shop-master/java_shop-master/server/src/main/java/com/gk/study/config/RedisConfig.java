package com.gk.study.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.embedding.redis.RedisEmbeddingStore;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

/**
 * LangChain4j 与 Redis 的 Bean：AI 客服记忆 + RAG 向量库。
 * <p>
 * RAG 的 {@link dev.langchain4j.store.embedding.redis.RedisEmbeddingStore} 依赖 <strong>Redis Stack</strong>
 *（RediSearch + RedisJSON）。未安装时请保持 {@code rag.redis.stack-enabled=false}，将使用内存向量库以便启动。
 */
@Configuration
public class RedisConfig {

    @Bean
    public ChatMemoryStore chatMemoryStore(
            StringRedisTemplate stringRedisTemplate,
            @Value("${app.redis.chat-memory.key-prefix:ai:chat:memory:}") String keyPrefix) {
        return new SpringRedisChatMemoryStore(stringRedisTemplate, keyPrefix);
    }

    @Bean
    @ConditionalOnProperty(name = "rag.redis.stack-enabled", havingValue = "true")
    public EmbeddingStore<TextSegment> redisStackEmbeddingStore(
            @Value("${spring.data.redis.host}") String host,
            @Value("${spring.data.redis.port}") int port,
            @Value("${spring.data.redis.password:}") String password,
            @Value("${rag.redis.index-name:java-shop-rag-index}") String indexName,
            @Value("${rag.redis.key-prefix:rag:embedding:}") String prefix,
            @Value("${rag.redis.embedding-dimension:1024}") int dimension) {
        var builder = RedisEmbeddingStore.builder()
                .host(host)
                .port(port)
                .indexName(indexName)
                .prefix(ensurePrefixEndsWithColon(prefix))
                .dimension(dimension)
                .metadataKeys(List.of("source"));
        if (password != null && !password.isBlank()) {
            builder.password(password);
        }
        return builder.build();
    }

    @Bean
    @ConditionalOnProperty(name = "rag.redis.stack-enabled", havingValue = "false", matchIfMissing = true)
    public EmbeddingStore<TextSegment> inMemoryEmbeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    private static String ensurePrefixEndsWithColon(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return "rag:embedding:";
        }
        return prefix.endsWith(":") ? prefix : prefix + ":";
    }
}
