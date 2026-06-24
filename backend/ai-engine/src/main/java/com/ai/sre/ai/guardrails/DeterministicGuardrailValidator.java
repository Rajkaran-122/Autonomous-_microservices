package com.ai.sre.ai.guardrails;

import com.ai.sre.common.event.AnalysisEvent.Recommendation;
import org.springframework.stereotype.Component;

@Component
public class DeterministicGuardrailValidator {

    /**
     * Validates an AI recommendation against hard-coded, deterministic rules.
     * @return null if valid, or a String reason if blocked.
     */
    public String validate(Recommendation rec) {
        if (rec == null || rec.actionType() == null || rec.targetResource() == null) {
            return "Invalid recommendation: actionType and targetResource cannot be null.";
        }

        String actionType = rec.actionType().toUpperCase();
        String target = rec.targetResource();

        switch (actionType) {
            case "SCALE_UP_PODS":
                return validateScaleUp(rec);
            case "POD_RESTART":
                return validatePodRestart(target);
            case "ROLLBACK":
                return validateRollback(target);
            default:
                // For unknown actions, we allow them to proceed to the standard RiskMatrixScorer
                // (though a stricter system might block by default).
                return null; 
        }
    }

    private String validateScaleUp(Recommendation rec) {
        if (rec.parameters() != null && rec.parameters().containsKey("replicas")) {
            try {
                int replicas = Integer.parseInt(rec.parameters().get("replicas").toString());
                if (replicas > 10) {
                    return "Deterministic Block: SCALE_UP_PODS requested replicas (" + replicas + ") exceeds maximum hard limit of 10.";
                }
            } catch (NumberFormatException e) {
                return "Deterministic Block: SCALE_UP_PODS parameter 'replicas' is not a valid integer.";
            }
        }
        return null;
    }

    private String validatePodRestart(String target) {
        if (!target.endsWith("-service")) {
            return "Deterministic Block: POD_RESTART target ('" + target + "') must end in '-service' to prevent core infrastructure restarts.";
        }
        return null;
    }

    private String validateRollback(String target) {
        // In a real system, this would query the Deployment API to ensure there is a previous ReplicaSet to rollback to.
        if ("core-db".equals(target)) {
            return "Deterministic Block: ROLLBACK on 'core-db' is strictly forbidden via automated agents.";
        }
        return null;
    }
}
