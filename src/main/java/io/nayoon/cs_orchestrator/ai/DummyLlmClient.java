package io.nayoon.cs_orchestrator.ai;

import io.nayoon.cs_orchestrator.domain.TriageResult;

import java.util.Random;

public class DummyLlmClient {
    private final Random random = new Random();

    public TriageResult triage(String latestMessage) {
        // 오늘은 고정 규칙으로 더미를 만들어도 됨
        // 메시지 내용 기반으로 아주 단순 분기
        boolean sensitive = latestMessage != null && latestMessage.toLowerCase().contains("password");
        boolean hasEvidence = latestMessage != null && latestMessage.contains("http");

        String emotion = (latestMessage != null && latestMessage.contains("화나")) ? "ANGRY" : "NEUTRAL";
        String risk = sensitive ? "HIGH" : "LOW";

        double confidence = sensitive ? 0.9 : 0.75 + (random.nextDouble() * 0.2);
        String intent = "GENERAL_INQUIRY";

        return new TriageResult(intent, Math.min(confidence, 0.99), risk, sensitive, emotion, hasEvidence);
    }
}

