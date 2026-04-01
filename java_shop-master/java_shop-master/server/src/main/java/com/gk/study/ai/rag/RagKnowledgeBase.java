package com.gk.study.ai.rag;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RagKnowledgeBase {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final RagProperties ragProperties;

    public RagKnowledgeBase(EmbeddingModel embeddingModel,
                            EmbeddingStore<TextSegment> embeddingStore,
                            RagProperties ragProperties) {
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
        this.ragProperties = ragProperties;
    }

    public List<TextSegment> search(String question) {
        if (question == null || question.isBlank()) {
            return Collections.emptyList();
        }

        var queryEmbedding = embeddingModel.embed(question).content();
        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(ragProperties.getTopK())
                .minScore(ragProperties.getMinScore())
                .build();

        EmbeddingSearchResult<TextSegment> result = embeddingStore.search(request);
        List<TextSegment> matched = result.matches().stream()
                .map(EmbeddingMatch::embedded)
                .collect(Collectors.toList());

        if (matched.isEmpty() && ragProperties.getMinScore() > 0D) {
            EmbeddingSearchRequest fallback = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(ragProperties.getTopK())
                    .minScore(0D)
                    .build();
            EmbeddingSearchResult<TextSegment> fallbackResult = embeddingStore.search(fallback);
            return fallbackResult.matches().stream()
                    .map(EmbeddingMatch::embedded)
                    .collect(Collectors.toList());
        }

        return matched;
    }
}
