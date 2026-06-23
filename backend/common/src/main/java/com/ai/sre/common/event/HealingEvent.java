package com.ai.sre.common.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Kafka event for self-healing action requests and results.
 * Published to topics: healing-commands (requests), healing-results (outcomes)
 * Consumed by: healing-service (commands), incident-service + notification-service (results)
 */
public record HealingEvent(
        UUID eventId,
        UUID healingActionId,
        UUID incidentId,
        UUID serviceId,
        String serviceName,

        // Action details
        String actionType,            // POD_RESTART, SCALE_UP, SCALE_DOWN, ROLLBACK, CACHE_CLEAR
        String targetNamespace,
        String targetResource,        // deployment name, pod name, etc.
        String status,                // PENDING, APPROVED, EXECUTING, COMPLETED, FAILED, ROLLED_BACK, REJECTED

        // Confidence & approval
        int confidenceScore,
        boolean requiresApproval,
        String approvedBy,

        // State snapshots
        Map<String, Object> beforeState,
        Map<String, Object> afterState,

        // Execution metadata
        boolean dryRun,
        String executionLog,
        long executionTimeMs,
        String failureReason,

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant timestamp
) {
    public HealingEvent {
        if (eventId == null) eventId = UUID.randomUUID();
        if (timestamp == null) timestamp = Instant.now();
    }
}
