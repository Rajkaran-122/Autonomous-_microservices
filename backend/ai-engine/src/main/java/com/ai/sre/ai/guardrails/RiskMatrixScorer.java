package com.ai.sre.ai.guardrails;

import com.ai.sre.ai.guardrails.model.PolicyRule;
import com.ai.sre.common.event.AnalysisEvent.Recommendation;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Implements the Risk Matrix Scoring system.
 * Combines AI confidence, action risk, and blast radius to produce a final risk score.
 */
@Component
public class RiskMatrixScorer {

    private final DeterministicGuardrailValidator validator;

    public RiskMatrixScorer(DeterministicGuardrailValidator validator) {
        this.validator = validator;
    }

    private static final Map<String, Integer> RISK_WEIGHTS = Map.of(
            "LOW", 10,
            "MEDIUM", 40,
            "HIGH", 70,
            "CRITICAL", 100
    );

    public RiskAssessment assess(Recommendation recommendation, PolicyRule policy, int blastRadius) {
        int confidence = recommendation.confidenceScore();
        int actionRiskWeight = RISK_WEIGHTS.getOrDefault(policy.getActionRiskLevel(), 50);
        
        // Blast radius score: 0 if 0 affected, up to 100 if > 10 affected
        int blastRadiusScore = Math.min(blastRadius * 10, 100);

        // Calculate combined score
        // Formula favors high confidence and penalizes high risk/blast radius
        double combinedScore = (confidence * 0.5) 
                             + ((100 - actionRiskWeight) * 0.3) 
                             + ((100 - blastRadiusScore) * 0.2);

        String decision;
        String reason;

        if (blastRadius > policy.getMaxBlastRadius()) {
            decision = "APPROVAL_REQUIRED";
            reason = "Blast radius (" + blastRadius + ") exceeds policy maximum (" + policy.getMaxBlastRadius() + ")";
        } else if (confidence >= policy.getAutoExecuteThreshold()) {
            decision = "AUTO_EXECUTE";
            reason = "High confidence score (" + confidence + ") meets auto-execute threshold";
        } else if (confidence >= policy.getApprovalThreshold()) {
            decision = "APPROVAL_REQUIRED";
            reason = "Confidence score (" + confidence + ") meets approval threshold but not auto-execute";
        } else {
            decision = "BLOCKED";
            reason = "Confidence score (" + confidence + ") is below approval threshold";
        }

        // Hard block for critical risk if confidence is not extremely high
        if ("CRITICAL".equals(policy.getActionRiskLevel()) && confidence < 95) {
            decision = "BLOCKED";
            reason = "Action has CRITICAL risk level and requires >=95% confidence";
        }

        // --- Deterministic Guardrail Override ---
        String validationError = validator.validate(recommendation);
        if (validationError != null) {
            decision = "BLOCKED";
            reason = validationError; // Overrides the LLM's confidence entirely
        }

        return new RiskAssessment(
                combinedScore,
                decision,
                reason,
                actionRiskWeight,
                blastRadiusScore
        );
    }

    public record RiskAssessment(
            double combinedScore,
            String decision, // AUTO_EXECUTE, APPROVAL_REQUIRED, BLOCKED
            String reason,
            int actionRiskWeight,
            int blastRadiusScore
    ) {}
}
