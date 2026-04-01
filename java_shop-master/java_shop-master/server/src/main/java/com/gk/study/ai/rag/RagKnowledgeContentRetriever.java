package com.gk.study.ai.rag;

import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 将现有向量检索 {@link RagKnowledgeBase} 适配为 LangChain4j {@link ContentRetriever}，
 * 供 {@code AiServices} 在每次对话中按需检索；检索结果只进入当轮提示，不写入 {@link dev.langchain4j.memory.ChatMemory}。
 */
@Component
public class RagKnowledgeContentRetriever implements ContentRetriever {

    private final RagKnowledgeBase ragKnowledgeBase;

    public RagKnowledgeContentRetriever(RagKnowledgeBase ragKnowledgeBase) {
        this.ragKnowledgeBase = ragKnowledgeBase;
    }

    @Override
    public List<Content> retrieve(Query query) {
        return ragKnowledgeBase.search(query.text()).stream()
                .map(Content::from)
                .collect(Collectors.toList());
    }
}
