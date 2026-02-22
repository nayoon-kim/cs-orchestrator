package io.nayoon.cs_orchestrator.service;

import io.nayoon.cs_orchestrator.domain.RoutingDecision;
import io.nayoon.cs_orchestrator.domain.TriageResult;

import java.util.Set;

public class RoutingPolicy {

    // HUMAN 강제 intent
    private static final Set<String> HUMAN_INTENTS = Set.of(
            "refund", "billing", "payment", "chargeback",
            "account", "password_reset", "account_lock",
            "legal", "compliance", "privacy", "gdpr",
            "harassment", "threat"
    );

    // AUTO 허용 intent (FAQ/정보성)
    private static final Set<String> AUTO_INTENTS = Set.of(
            "faq", "faq_shipping", "faq_delivery", "faq_product", "howto", "guidance"
    );

    public static RoutingDecision hardGate(TriageResult r) {
        if (r == null) return RoutingDecision.HUMAN;

        String intent = norm(r.getIntent());
        String risk = norm(r.getRiskLevel());
        String emotion = norm(r.getEmotion());

        // 0) 명백히 위험하거나 민감하면 AUTO 금지
        if (r.isSensitive()) return RoutingDecision.HUMAN;
        if ("high".equals(risk)) return RoutingDecision.HUMAN;

        // 1) 특정 intent는 무조건 HUMAN
        if (HUMAN_INTENTS.contains(intent)) return RoutingDecision.HUMAN;

        // 2) 감정이 격하면 AUTO 금지 (불만 응대는 사람 승인 권장)
        boolean angry = "angry".equals(emotion);
        if (angry) {
            // angry + low risk면 초안 정도는 만들 수 있으니 DRAFT로
            return RoutingDecision.DRAFT;
        }

        // 3) AUTO 허용 조건 (아주 보수적으로)
        boolean isAutoIntent = AUTO_INTENTS.contains(intent) || intent.startsWith("faq_");
        boolean highConfidence = r.getConfidence() >= 0.85;
        boolean lowRisk = "low".equals(risk) || risk.isBlank(); // risk 없으면 보수적으로 DRAFT로 바꿔도 됨
        boolean hasEvidence = r.isHasEvidence();

        if (isAutoIntent && highConfidence && lowRisk && hasEvidence) {
            return RoutingDecision.AUTO;
        }

        // 4) 나머지는 DRAFT
        return RoutingDecision.DRAFT;
    }

    private static String norm(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }
}

