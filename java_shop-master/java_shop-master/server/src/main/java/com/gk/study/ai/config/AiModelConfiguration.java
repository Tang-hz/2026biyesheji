package com.gk.study.ai.config;

import com.gk.study.ai.AiReactService;
import com.gk.study.ai.rag.RagKnowledgeContentRetriever;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.gk.study.ai.tool.AiMemberTool;
import com.gk.study.ai.tool.AiOrderRedeemTool;
import com.gk.study.ai.tool.AiOrderTool;

import java.time.Duration;

/**
 * 统一装配：按 {@link AiProperties#getProvider()} 选择阿里百炼（OpenAI 兼容），
 * 统一使用 RAG + 工具的 {@link  } 服务。
 */
@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class AiModelConfiguration {

    @Bean
    public StreamingChatLanguageModel streamingChatLanguageModel(AiProperties ai) {
        if (isBailian(ai)) {
            return buildBailianStreamingChat(ai.getBailian());
        }
        return buildOllamaStreamingChat(ai.getOllama());
    }

    @Bean
    public ChatLanguageModel chatLanguageModel(AiProperties ai) {
        if (isBailian(ai)) {
            return buildBailianNonStreamingChat(ai.getBailian());
        }
        return buildOllamaNonStreamingChat(ai.getOllama());
    }

    @Bean
    public EmbeddingModel embeddingModel(AiProperties ai) {
        if (isBailian(ai)) {
            return buildBailianEmbedding(ai.getBailian());
        }
        return buildOllamaEmbedding(ai.getOllama());
    }

    @Bean
    public AiReactService aiReactService(
            StreamingChatLanguageModel streamingChatLanguageModel,
            ChatMemoryProvider chatMemoryProvider,
            RagKnowledgeContentRetriever ragKnowledgeContentRetriever,
            AiOrderTool aiOrderTool,
            AiOrderRedeemTool aiOrderRedeemTool,
            AiMemberTool aiMemberTool) {
        return AiServices.builder(AiReactService.class)
                .streamingChatLanguageModel(streamingChatLanguageModel)
                .chatMemoryProvider(chatMemoryProvider)
                .contentRetriever(ragKnowledgeContentRetriever)
                .tools(aiOrderTool, aiOrderRedeemTool, aiMemberTool)
                .build();
    }

    private static boolean isBailian(AiProperties ai) {
        return "bailian".equalsIgnoreCase(ai.getProvider());
    }

    // ========== Ollama Streaming (RagAnswerAi 用) ==========

    private static StreamingChatLanguageModel buildOllamaStreamingChat(AiProperties.Ollama o) {
        var builder = OllamaStreamingChatModel.builder()
                .baseUrl(o.getBaseUrl())
                .modelName(o.getChatModel());
        if (o.getTemperature() != null) builder.temperature(o.getTemperature());
        if (o.getTimeoutSeconds() != null) builder.timeout(Duration.ofSeconds(o.getTimeoutSeconds()));
        return builder.build();
    }

    // ========== Ollama Non-Streaming (ReActAgent 用) ==========

    private static ChatLanguageModel buildOllamaNonStreamingChat(AiProperties.Ollama o) {
        // 使用 OllamaChatModel 的 builder
        var builder = dev.langchain4j.model.ollama.OllamaChatModel.builder()
                .baseUrl(o.getBaseUrl())
                .modelName(o.getChatModel());
        if (o.getTemperature() != null) builder.temperature(o.getTemperature());
        if (o.getTimeoutSeconds() != null) builder.timeout(Duration.ofSeconds(o.getTimeoutSeconds()));
        return builder.build();
    }

    // ========== Bailian Streaming (RagAnswerAi 用) ==========

    private static StreamingChatLanguageModel buildBailianStreamingChat(AiProperties.Bailian b) {
        var builder = OpenAiStreamingChatModel.builder()
                .baseUrl(b.getBaseUrl())
                .apiKey(b.getApiKey())
                .modelName(b.getChatModel());
        if (b.getTemperature() != null) builder.temperature(b.getTemperature());
        if (b.getTimeoutSeconds() != null) builder.timeout(Duration.ofSeconds(b.getTimeoutSeconds()));
        return builder.build();
    }

    // ========== Bailian Non-Streaming (ReActAgent 用) ==========

    private static ChatLanguageModel buildBailianNonStreamingChat(AiProperties.Bailian b) {
        var builder = dev.langchain4j.model.openai.OpenAiChatModel.builder()
                .baseUrl(b.getBaseUrl())
                .apiKey(b.getApiKey())
                .modelName(b.getChatModel());
        if (b.getTemperature() != null) builder.temperature(b.getTemperature());
        if (b.getTimeoutSeconds() != null) builder.timeout(Duration.ofSeconds(b.getTimeoutSeconds()));
        return builder.build();
    }

    // ========== Embedding ==========

    private static EmbeddingModel buildOllamaEmbedding(AiProperties.Ollama o) {
        var builder = OllamaEmbeddingModel.builder()
                .baseUrl(o.getBaseUrl())
                .modelName(o.getEmbeddingModel());
        if (o.getTimeoutSeconds() != null) builder.timeout(Duration.ofSeconds(o.getTimeoutSeconds()));
        return builder.build();
    }

    private static EmbeddingModel buildBailianEmbedding(AiProperties.Bailian b) {
        var builder = OpenAiEmbeddingModel.builder()
                .baseUrl(b.getBaseUrl())
                .apiKey(b.getApiKey())
                .modelName(b.getEmbeddingModel());
        if (b.getTimeoutSeconds() != null) builder.timeout(Duration.ofSeconds(b.getTimeoutSeconds()));
        return builder.build();
    }
}