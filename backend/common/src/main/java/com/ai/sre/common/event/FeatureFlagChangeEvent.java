package com.ai.sre.common.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import java.util.UUID;

/**
 * Kafka event emitted when a feature flag is changed.
 * Published to topic: feature-flag-changes
 * Consumed by: all services (for cache invalidation)
 */
public record FeatureFlagChangeEvent(
        UUID eventId,
        UUID flagId,
        String flagKey,
        String changeType,            // CREATED, ENABLED, DISABLED, PERCENTAGE_CHANGED, KILL_SWITCH_ON, KILL_SWITCH_OFF, DELETED
        Boolean previousEnabled,
        Boolean newEnabled,
        Integer previousPercentage,
        Integer newPercentage,
        String changedBy,
        String reason,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant timestamp
) {
    public FeatureFlagChangeEvent {
        if (eventId == null) eventId = UUID.randomUUID();
        if (timestamp == null) timestamp = Instant.now();
    }
}
