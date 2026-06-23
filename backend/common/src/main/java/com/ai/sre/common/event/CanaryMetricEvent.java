package com.ai.sre.common.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Kafka event emitted when a canary deployment captures metrics at each step.
 * Published to topic: canary-metrics
 * Consumed by: dashboard (real-time updates)
 */
public record CanaryMetricEvent(
        UUID eventId,
        UUID canaryDeploymentId,
        UUID serviceId,
        String serviceName,
        int stepNumber,
        int trafficPercentage,

        // Baseline metrics
        double baselineErrorRate,
        double baselineP99Ms,
        double baselineRps,

        // Canary metrics
        double canaryErrorRate,
        double canaryP99Ms,
        double canaryRps,

        // Deltas
        double errorRateDelta,
        double latencyDeltaMs,

        // Verdict
        String verdict,               // PASS, WARN, FAIL
        String canaryStatus,           // MONITORING, PROMOTING, ROLLED_BACK

        Map<String, Object> additionalMetrics,

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant timestamp
) {
    public CanaryMetricEvent {
        if (eventId == null) eventId = UUID.randomUUID();
        if (timestamp == null) timestamp = Instant.now();
    }
}
