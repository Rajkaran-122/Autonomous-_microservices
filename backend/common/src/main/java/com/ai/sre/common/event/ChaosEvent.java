package com.ai.sre.common.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Kafka event emitted during chaos engineering experiments.
 * Published to topic: chaos-events
 * Consumed by: dashboard, notification-service
 */
public record ChaosEvent(
        UUID eventId,
        UUID experimentId,
        UUID runId,
        UUID serviceId,
        String serviceName,
        String experimentName,
        String experimentType,        // POD_KILL, NETWORK_LATENCY, CPU_STRESS, MEMORY_STRESS, POD_FAILURE
        String phase,                 // PRE_CHECK, INJECTING, MONITORING, POST_CHECK, COMPLETED, ABORTED
        boolean steadyStateValid,
        boolean selfHealingTriggered,
        long selfHealingResponseTimeMs,
        String verdict,               // PASSED, FAILED, ABORTED
        String message,
        Map<String, Object> metrics,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant timestamp
) {
    public ChaosEvent {
        if (eventId == null) eventId = UUID.randomUUID();
        if (timestamp == null) timestamp = Instant.now();
    }
}
