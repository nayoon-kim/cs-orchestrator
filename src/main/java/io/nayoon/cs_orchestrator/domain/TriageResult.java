package io.nayoon.cs_orchestrator.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TriageResult {
    private String intent;
    private double confidence;

    @JsonProperty("risk_level")
    private String riskLevel;

    private boolean sensitive;

    @JsonProperty("has_evidence")
    private boolean hasEvidence;

    private String emotion;

    @JsonProperty("recommended_action")
    private String recommendedAction;

    private String rationale;

    public TriageResult() {}

    public TriageResult(String intent, double confidence, String riskLevel, boolean sensitive, String emotion, boolean hasEvidence) {
        this.intent = intent;
        this.confidence = confidence;
        this.riskLevel = riskLevel;
        this.sensitive = sensitive;
        this.emotion = emotion;
        this.hasEvidence = hasEvidence;
    }

    public String getIntent() {
        return intent;
    }

    public double getConfidence() {
        return confidence;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public boolean isSensitive() {
        return sensitive;
    }

    public boolean isHasEvidence() {
        return hasEvidence;
    }

    public String getEmotion() {
        return emotion;
    }

    public String getRecommendedAction() {
        return recommendedAction;
    }

    public String getRationale() {
        return rationale;
    }
}

