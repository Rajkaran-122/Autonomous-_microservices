package com.ai.sre.common.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Kafka event emitted when an incident is detected by the incident-service.
 * Published to topic: incident-events
 * Consumed by: ai-engine, notification-service
 */
public record IncidentEvent(
        UUID eventId,
        UUID incidentId,
        String title,
        String description,
        String severity,          // P1, P2, P3, P4
        String status,            // DETECTED, ANALYZING, ACTIVE, REMEDIATING, RESOLVED, CLOSED
        UUID serviceId,
        String serviceName,
        String namespace,
        String correlationKey,
        List<String> affectedServices,
        Map<String, Object> detectionMetadata,
        double errorRate,
        double latencyP99Ms,
        int podRestartCount,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant detectedAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant timestamp
) {
    public IncidentEvent {
        if (eventId == null) eventId = UUID.randomUUID();
        if (timestamp == null) timestamp = Instant.now();
        if (status == null) status = "DETECTED";
    }
}
