package com.ai.sre.common.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import java.util.UUID;

/**
 * Kafka event emitted when the policy engine makes a decision about an AI-recommended action.
 * Published to topic: policy-decisions
 * Consumed by: audit-service, notification-service
 */
public record PolicyDecisionEvent(
        UUID eventId,
        UUID decisionId,
        UUID healingActionId,
        UUID incidentId,
        UUID serviceId,
        String serviceName,
        String actionType,
        int aiConfidenceScore,
        String actionRiskLevel,       // LOW, MEDIUM, HIGH, CRITICAL
        int blastRadiusCount,
        double combinedRiskScore,
        String decision,              // AUTO_EXECUTE, APPROVAL_REQUIRED, BLOCKED, MANUAL_OVERRIDE
        String decisionReason,
        UUID matchedPolicyId,
        String parameters,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant timestamp
) {
    public PolicyDecisionEvent {
        if (eventId == null) eventId = UUID.randomUUID();
        if (timestamp == null) timestamp = Instant.now();
    }
}
