package io.nayoon.cs_orchestrator.service;

import io.nayoon.cs_orchestrator.domain.RoutingDecision;
import io.nayoon.cs_orchestrator.domain.TriageResult;

public final class DummyRoutingPolicy {

    public static RoutingDecision decideRouting(TriageResult r) {
        // hard rule 예시
        if (r.isSensitive()) return RoutingDecision.HUMAN;
        if ("HIGH".equals(r.getRiskLevel())) return RoutingDecision.HUMAN;
        if (r.getConfidence() >= 0.85 && !"ANGRY".equals(r.getEmotion())) return RoutingDecision.AUTO;
        return RoutingDecision.DRAFT;
    }

    public static int computePriority(TriageResult r) {
        // 숫자 클수록 더 높은 우선순위라고 가정
        int p = 0;

        // risk
        p += switch (r.getRiskLevel()) {
            case "HIGH" -> 70;
            case "MEDIUM" -> 40;
            default -> 10;
        };

        // sensitive
        if (r.isSensitive()) p += 20;

        // evidence
        if (r.isHasEvidence()) p += 10;

        // emotion (예시)
        if ("ANGRY".equals(r.getEmotion())) p += 10;

        // confidence (가벼운 가중치)
        p += (int) Math.round(r.getConfidence() * 10);

        return Math.min(p, 100);
    }

    private DummyRoutingPolicy() {}
}

