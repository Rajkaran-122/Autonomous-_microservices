package com.ai.sre.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST API representation of an incident.
 */
public record IncidentDTO(
        UUID id,
        String title,
        String description,
        String severity,
        String status,
        UUID serviceId,
        String serviceName,
        String rootCause,
        UUID aiAnalysisId,
        String correlationKey,
        String createdBy,
        Integer mttrSeconds,
        List<TimelineEventDTO> timeline,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant detectedAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant resolvedAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant createdAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant updatedAt
) {
    public record TimelineEventDTO(
            UUID id,
            String eventType,
            String description,
            Map<String, Object> metadata,
            @JsonFormat(shape = JsonFormat.Shape.STRING)
            Instant createdAt
    ) {}
}
