package com.gk.study.ai.rag;

import com.gk.study.ai.config.AiProperties;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentByCharacterSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Configuration
@EnableConfigurationProperties(RagProperties.class)
public class RagConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RagConfiguration.class);

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    @Bean
    public RagKnowledgeBase ragKnowledgeBase(EmbeddingModel embeddingModel,
                                             EmbeddingStore<TextSegment> embeddingStore,
                                             RagProperties ragProperties,
                                             AiProperties aiProperties) throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        String configuredPattern = ragProperties.getKnowledgePattern() == null
                ? "classpath*:Rag/Knowledge/*.md"
                : ragProperties.getKnowledgePattern();
        Resource[] resources = resolver.getResources(Objects.requireNonNull(configuredPattern));
        if (resources.length == 0) {
            // 兼容用户将目录放在小写路径的情况
            resources = resolver.getResources("classpath*:rag/knowledge/*.md");
        }
        if (resources.length == 0) {
            // 兜底尝试大写路径
            resources = resolver.getResources("classpath*:Rag/Knowledge/*.md");
        }
        if (resources.length == 0) {
            log.warn("RAG knowledge markdown files not found, pattern={}", configuredPattern);
            return new RagKnowledgeBase(embeddingModel, embeddingStore, ragProperties);
        }

        DocumentByCharacterSplitter splitter = new DocumentByCharacterSplitter(
                ragProperties.getMaxSegmentSize(),
                ragProperties.getMaxOverlapSize()
        );

        List<TextSegment> allSegments = new ArrayList<>();
        for (Resource resource : resources) {
            String source = resource.getFilename() == null ? "unknown.md" : resource.getFilename();
            String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            if (content.isBlank()) {
                continue;
            }

            // Markdown 以纯文本方式入库，同时保留来源文件名用于上下文展示
            Document document = Document.from(content, Metadata.from("source", source));
            allSegments.addAll(splitter.split(document));
        }

        if (!allSegments.isEmpty()) {
            embeddingStore.addAll(embeddingModel.embedAll(allSegments).content(), allSegments);
            log.info("RAG knowledge loaded: provider={}, embeddingModel={}, files={}, segments={}",
                    aiProperties.getProvider(), aiProperties.resolveEmbeddingModel(), resources.length, allSegments.size());
        } else {
            log.warn("RAG knowledge loaded with 0 segments.");
        }

        return new RagKnowledgeBase(embeddingModel, embeddingStore, ragProperties);
    }
}
