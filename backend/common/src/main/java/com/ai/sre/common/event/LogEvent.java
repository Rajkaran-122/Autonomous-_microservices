package com.ai.sre.common.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Kafka event emitted when a raw log entry is received from a monitored service.
 * Published to topic: raw-log-events
 * Consumed by: log-ingestion-service
 */
public record LogEvent(
        UUID eventId,
        String serviceName,
        String namespace,
        String logLevel,      // DEBUG, INFO, WARN, ERROR, FATAL
        String message,
        String rawPayload,
        String logFormat,     // JSON, LOGFMT, PLAINTEXT
        String traceId,
        String spanId,
        Map<String, String> metadata,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant timestamp
) {
    public LogEvent {
        if (eventId == null) eventId = UUID.randomUUID();
        if (timestamp == null) timestamp = Instant.now();
        if (logFormat == null) logFormat = "PLAINTEXT";
    }
}
