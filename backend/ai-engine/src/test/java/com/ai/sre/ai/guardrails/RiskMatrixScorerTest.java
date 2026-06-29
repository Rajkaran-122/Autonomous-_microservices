package com.ai.sre.ai.guardrails;

import com.ai.sre.ai.guardrails.model.PolicyRule;
import com.ai.sre.common.event.AnalysisEvent.Recommendation;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class RiskMatrixScorerTest {

    private final DeterministicGuardrailValidator validator = new DeterministicGuardrailValidator();
    private final RiskMatrixScorer scorer = new RiskMatrixScorer(validator);

    @Test
    void testAssess_DeterministicBlockOverridesLLM() {
        // High confidence from LLM
        Recommendation rec = new Recommendation("SCALE_UP_PODS", "payment-service", "desc", 99, Map.of("replicas", "100"));
        PolicyRule policy = new PolicyRule();
        policy.setActionRiskLevel("LOW");
        policy.setMaxBlastRadius(5);
        policy.setAutoExecuteThreshold(90);
        policy.setApprovalThreshold(70);

        RiskMatrixScorer.RiskAssessment assessment = scorer.assess(rec, policy, 1);

        assertEquals("BLOCKED", assessment.decision());
        assertTrue(assessment.reason().contains("exceeds maximum hard limit of 10"), "Should be blocked by deterministic guardrail");
    }
}
