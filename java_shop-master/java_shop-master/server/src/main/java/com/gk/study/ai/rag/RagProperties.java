package com.gk.study.ai.rag;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rag")
public class RagProperties {

    /**
     * 知识库 Markdown 路径（支持 classpath*: 通配）
     */
    private String knowledgePattern = "classpath*:Rag/Knowledge/*.md";

    /**
     * 分块大小（字符数）
     */
    private int maxSegmentSize = 600;

    /**
     * 分块重叠（字符数）
     */
    private int maxOverlapSize = 80;

    /**
     * 每次检索返回的片段数
     */
    private int topK = 6;

    /**
     * 向量相似度最小阈值
     */
    private double minScore = 0.5D;

    public String getKnowledgePattern() {
        return knowledgePattern;
    }

    public void setKnowledgePattern(String knowledgePattern) {
        this.knowledgePattern = knowledgePattern;
    }

    public int getMaxSegmentSize() {
        return maxSegmentSize;
    }

    public void setMaxSegmentSize(int maxSegmentSize) {
        this.maxSegmentSize = maxSegmentSize;
    }

    public int getMaxOverlapSize() {
        return maxOverlapSize;
    }

    public void setMaxOverlapSize(int maxOverlapSize) {
        this.maxOverlapSize = maxOverlapSize;
    }

    public int getTopK() {
        return topK;
    }

    public void setTopK(int topK) {
        this.topK = topK;
    }

    public double getMinScore() {
        return minScore;
    }

    public void setMinScore(double minScore) {
        this.minScore = minScore;
    }
}
