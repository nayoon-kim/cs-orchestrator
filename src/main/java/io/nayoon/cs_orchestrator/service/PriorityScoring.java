package io.nayoon.cs_orchestrator.service;

import io.nayoon.cs_orchestrator.domain.RoutingDecision;
import io.nayoon.cs_orchestrator.domain.TriageResult;

public class PriorityScoring {

    public static int scorePriority(TriageResult r, RoutingDecision decision) {
        int score = 0;

        // 1) 라우팅별 기본 점수
        switch (decision) {
            case HUMAN -> score += 1000;
            case DRAFT -> score += 700;
            case AUTO  -> score += 400;
        }

        if (r == null) return score;

        String risk = norm(r.getRiskLevel());
        String emotion = norm(r.getEmotion());

        // 2) 리스크 가중치
        if ("high".equals(risk)) score += 300;       // 원래 hardGate에서 HUMAN으로 빠지지만, 방어적
        else if ("medium".equals(risk)) score += 150;

        // 3) 감정 가중치
        if ("angry".equals(emotion)) score += 120;
        else if ("sad".equals(emotion)) score += 50;

        // 4) confidence 가중치 (낮을수록 상담사 필요)
        // confidence 1.0 -> +0, 0.0 -> +200
        int confPenalty = (int) Math.round((1.0 - clamp01(r.getConfidence())) * 200);
        score += confPenalty;

        // 5) 민감정보는 상담사 우선
        if (r.isSensitive()) score += 250;

        // 6) 결제/계정 같은 키워드 intent면 가중 (혹시 HUMAN_INTENTS에 누락되었을 때 대비)
        String intent = norm(r.getIntent());
        if (intent.contains("billing") || intent.contains("payment")) score += 200;
        if (intent.contains("account")) score += 180;
        if (intent.contains("refund")) score += 220;

        // 7) 근거 없으면 상담사 우선
        if (!r.isHasEvidence()) score += 120;

        // 점수 범위 안정화
        if (score < 0) score = 0;
        return score;
    }

    private static double clamp01(double v) {
        if (v < 0) return 0;
        if (v > 1) return 1;
        return v;
    }

    private static String norm(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }
}

