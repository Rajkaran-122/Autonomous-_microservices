package com.ai.sre.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * REST API representation of a registered service.
 */
public record ServiceDTO(
        UUID id,
        String name,
        String namespace,
        String serviceType,
        String tier,
        String owner,
        double sloTarget,
        String healthStatus,
        Map<String, Object> metadata,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant createdAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant updatedAt
) {}
