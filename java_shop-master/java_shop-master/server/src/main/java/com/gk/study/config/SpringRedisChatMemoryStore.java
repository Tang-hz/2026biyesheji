package com.gk.study.config;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.ArrayList;
import java.util.List;

/**
 * 使用 Spring Data Redis 的 {@link StringRedisTemplate} 持久化 LangChain4j 对话，
 * 与 {@code spring.data.redis}（含密码、database）一致。
 * <p>
 * LangChain4j 0.36 的 {@code RedisChatMemoryStore} 在仅配置密码时不会传给 Jedis，故此处自实现。
 */
public class SpringRedisChatMemoryStore implements ChatMemoryStore {

    private final StringRedisTemplate redis;
    private final String keyPrefix;

    public SpringRedisChatMemoryStore(StringRedisTemplate redis, String keyPrefix) {
        this.redis = redis;
        this.keyPrefix = keyPrefix.endsWith(":") ? keyPrefix : keyPrefix + ":";
    }

    private String key(Object memoryId) {
        return keyPrefix + memoryId;
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String json = redis.opsForValue().get(key(memoryId));
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        return ChatMessageDeserializer.messagesFromJson(json);
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String json = ChatMessageSerializer.messagesToJson(messages);
        redis.opsForValue().set(key(memoryId), json);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        redis.delete(key(memoryId));
    }
}
