package com.gk.study.ai.config;

import com.gk.study.ai.CustomerServiceAi;
import com.gk.study.ai.RagAnswerAi;
import com.gk.study.ai.rag.RagKnowledgeContentRetriever;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.gk.study.ai.tool.AiOrderTool;

import java.time.Duration;

/**
 * 统一装配：按 {@link AiProperties#getProvider()} 选择阿里百炼（OpenAI 兼容），
 * 供非 RAG {@code /stream} 与 RAG {@code /rag/stream} 共用，行为与拆分配置类时一致。
 */
@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class AiModelConfiguration {

    @Bean
    public StreamingChatLanguageModel streamingChatLanguageModel(AiProperties ai) {
        if (isBailian(ai)) {
            return buildBailianChat(ai.getBailian());
        }
        return buildOllamaChat(ai.getOllama());
    }

    @Bean
    public EmbeddingModel embeddingModel(AiProperties ai) {
        if (isBailian(ai)) {
            return buildBailianEmbedding(ai.getBailian());
        }
        return buildOllamaEmbedding(ai.getOllama());
    }

    /**
     * 客服工具注册在 {@link AiOrderTool}：商品检索 {@code searchThingsByKeyword}、
     * 订单查询 {@code getUserOrderByOrderNumber}、下单 {@code orderByThingTitle}。
     */
    @Bean
    public CustomerServiceAi customerServiceAi(
            StreamingChatLanguageModel streamingChatLanguageModel,
            ChatMemoryProvider chatMemoryProvider,
            AiOrderTool aiOrderTool) {
        return AiServices.builder(CustomerServiceAi.class)
                .streamingChatLanguageModel(streamingChatLanguageModel)
                .chatMemoryProvider(chatMemoryProvider)
                .tools(aiOrderTool)
                .build();
    }

    @Bean
    public RagAnswerAi ragAnswerAi(
            StreamingChatLanguageModel streamingChatLanguageModel,
            ChatMemoryProvider chatMemoryProvider,
            RagKnowledgeContentRetriever ragKnowledgeContentRetriever,
            AiOrderTool aiOrderTool) {
        return AiServices.builder(RagAnswerAi.class)
                .streamingChatLanguageModel(streamingChatLanguageModel)
                .chatMemoryProvider(chatMemoryProvider)
                .contentRetriever(ragKnowledgeContentRetriever)
                .tools(aiOrderTool)
                .build();
    }

    private static boolean isBailian(AiProperties ai) {
        return "bailian".equalsIgnoreCase(ai.getProvider());
    }

    private static StreamingChatLanguageModel buildOllamaChat(AiProperties.Ollama o) {
        var builder = OllamaStreamingChatModel.builder()
                .baseUrl(o.getBaseUrl())
                .modelName(o.getChatModel());
        if (o.getTemperature() != null) {
            builder.temperature(o.getTemperature());
        }
        if (o.getTimeoutSeconds() != null) {
            builder.timeout(Duration.ofSeconds(o.getTimeoutSeconds()));
        }
        return builder.build();
    }

    private static EmbeddingModel buildOllamaEmbedding(AiProperties.Ollama o) {
        var builder = OllamaEmbeddingModel.builder()
                .baseUrl(o.getBaseUrl())
                .modelName(o.getEmbeddingModel());
        if (o.getTimeoutSeconds() != null) {
            builder.timeout(Duration.ofSeconds(o.getTimeoutSeconds()));
        }
        return builder.build();
    }

    private static StreamingChatLanguageModel buildBailianChat(AiProperties.Bailian b) {
        var builder = OpenAiStreamingChatModel.builder()
                .baseUrl(b.getBaseUrl())
                .apiKey(b.getApiKey())
                .modelName(b.getChatModel());
        if (b.getTemperature() != null) {
            builder.temperature(b.getTemperature());
        }
        if (b.getTimeoutSeconds() != null) {
            builder.timeout(Duration.ofSeconds(b.getTimeoutSeconds()));
        }
        return builder.build();
    }

    private static EmbeddingModel buildBailianEmbedding(AiProperties.Bailian b) {
        var builder = OpenAiEmbeddingModel.builder()
                .baseUrl(b.getBaseUrl())
                .apiKey(b.getApiKey())
                .modelName(b.getEmbeddingModel());
        if (b.getTimeoutSeconds() != null) {
            builder.timeout(Duration.ofSeconds(b.getTimeoutSeconds()));
        }
        return builder.build();
    }
}
