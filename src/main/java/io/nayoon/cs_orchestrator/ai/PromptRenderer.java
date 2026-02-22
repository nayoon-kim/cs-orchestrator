package io.nayoon.cs_orchestrator.ai;

import org.springframework.stereotype.Component;

@Component
public class PromptRenderer {

    private static final String PLACEHOLDER = "{raw_customer_message}";

    public String render(String template, String rawCustomerMessage) {
        String safe = normalize(rawCustomerMessage);
        return template.replace(PLACEHOLDER, safe);
    }

    private String normalize(String s) {
        if (s == null) return "";
        // 너무 긴 입력은 잘라서 비용/지연/프롬프트 오염 방지
        int maxChars = 4000;
        String trimmed = s.length() > maxChars ? s.substring(0, maxChars) : s;

        // triple quotes 깨는 케이스 방지용으로 최소한의 정규화
        return trimmed.replace("\u0000", "");
    }
}
