package com.gk.study.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 统一 AI 提供方与模型参数（Ollama / 阿里百炼 OpenAI 兼容）。
 */
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    /**
     * ollama：本地 Ollama<br>
     * bailian：阿里云 DashScope 百炼（OpenAI 兼容端点）
     */
    private String provider = "ollama";

    private Ollama ollama = new Ollama();
    private Bailian bailian = new Bailian();

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Ollama getOllama() {
        return ollama;
    }

    public void setOllama(Ollama ollama) {
        this.ollama = ollama;
    }

    public Bailian getBailian() {
        return bailian;
    }

    public void setBailian(Bailian bailian) {
        this.bailian = bailian;
    }

    /** 当前提供方下的对话模型名 */
    public String resolveChatModel() {
        return "bailian".equalsIgnoreCase(provider) ? bailian.getChatModel() : ollama.getChatModel();
    }

    /** 当前提供方下的 Embedding 模型名 */
    public String resolveEmbeddingModel() {
        return "bailian".equalsIgnoreCase(provider) ? bailian.getEmbeddingModel() : ollama.getEmbeddingModel();
    }

    public static class Ollama {

        private String baseUrl = "http://localhost:11434";
        private String chatModel = "qwen3:8b";
        private String embeddingModel = "bge-m3:latest";
        /** 可选，未设置则使用各模型默认 */
        private Double temperature;
        /** 请求超时（秒），可选 */
        private Integer timeoutSeconds;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getChatModel() {
            return chatModel;
        }

        public void setChatModel(String chatModel) {
            this.chatModel = chatModel;
        }

        public String getEmbeddingModel() {
            return embeddingModel;
        }

        public void setEmbeddingModel(String embeddingModel) {
            this.embeddingModel = embeddingModel;
        }

        public Double getTemperature() {
            return temperature;
        }

        public void setTemperature(Double temperature) {
            this.temperature = temperature;
        }

        public Integer getTimeoutSeconds() {
            return timeoutSeconds;
        }

        public void setTimeoutSeconds(Integer timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
        }
    }

    public static class Bailian {
        private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
        private String apiKey = "";
        private String chatModel = "qwen-turbo";
        private String embeddingModel = "text-embedding-v3";
        private Double temperature;
        private Integer timeoutSeconds;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getChatModel() {
            return chatModel;
        }

        public void setChatModel(String chatModel) {
            this.chatModel = chatModel;
        }

        public String getEmbeddingModel() {
            return embeddingModel;
        }

        public void setEmbeddingModel(String embeddingModel) {
            this.embeddingModel = embeddingModel;
        }

        public Double getTemperature() {
            return temperature;
        }

        public void setTemperature(Double temperature) {
            this.temperature = temperature;
        }

        public Integer getTimeoutSeconds() {
            return timeoutSeconds;
        }

        public void setTimeoutSeconds(Integer timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
        }
    }
}
