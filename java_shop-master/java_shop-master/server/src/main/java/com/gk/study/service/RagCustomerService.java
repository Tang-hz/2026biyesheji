package com.gk.study.service;

import com.gk.study.ai.RagAnswerAi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import java.util.Locale;
import java.util.Set;

@Service
public class RagCustomerService {

    private static final Set<String> FAREWELL_PHRASES = Set.of(
            "再见", "再见了", "再见啦",
            "拜拜", "拜拜啦", "拜拜了",
            "bye", "goodbye", "byebye",
            "88", "886",
            "回见", "下次见"
    );

    private static final String FAREWELL_REPLY = "感谢您的咨询，祝您生活愉快。后续有订单、物流或售后问题，随时来找我。";
    private static final Logger log = LoggerFactory.getLogger(RagCustomerService.class);

    private final RagAnswerAi ragAnswerAi;

    public RagCustomerService(RagAnswerAi ragAnswerAi) {
        this.ragAnswerAi = ragAnswerAi;
    }

    public Flux<String> chatWithRag(String userQuestion) {
        return chatWithRag(userQuestion, "guest");
    }

    /**
     * RAG + 多轮记忆：用户原话进入 ChatMemory；知识仅由 ContentRetriever 每轮检索注入，不写入记忆。
     */
    public Flux<String> chatWithRag(String userQuestion, String memoryId) {
        String question = userQuestion == null ? "" : userQuestion.trim();
        if (question.isEmpty()) {
            return Flux.just("尊敬的用户你好，请问有什么需要帮到您？");
        }
        if (isFarewellOnly(question)) {
            return Flux.just(FAREWELL_REPLY);
        }
        String id = (memoryId == null || memoryId.isBlank()) ? "guest" : memoryId.trim();
        log.debug("RAG chat memoryId={}", id);
        return ragAnswerAi.chat(id, question);
    }

    private boolean isFarewellOnly(String question) {
        String normalized = normalizeFarewellPhrase(question);
        return FAREWELL_PHRASES.contains(normalized);
    }

    private String normalizeFarewellPhrase(String question) {
        if (question == null) {
            return "";
        }
        String n = question.toLowerCase(Locale.ROOT).trim();
        n = n.replaceAll("^[\\s！!。.，,~～…]+", "");
        n = n.replaceAll("[\\s！!。.，,~～…]+$", "");
        return n;
    }
}
