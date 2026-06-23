package com.ai.sre.common.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import java.util.UUID;

/**
 * Kafka event emitted when an SLO violation is detected (burn rate exceeds threshold).
 * Published to topic: slo-violations
 * Consumed by: notification-service, incident-service
 */
public record SloViolationEvent(
        UUID eventId,
        UUID sloId,
        UUID serviceId,
        String serviceName,
        String sloName,
        String violationType,         // FAST_BURN, SLOW_BURN, BUDGET_EXHAUSTED, SLO_BREACH
        String severity,              // P1, P2, P3
        double burnRate,
        double errorBudgetRemainingPct,
        double currentSliPct,
        double targetSloPct,
        String message,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant timestamp
) {
    public SloViolationEvent {
        if (eventId == null) eventId = UUID.randomUUID();
        if (timestamp == null) timestamp = Instant.now();
    }
}
