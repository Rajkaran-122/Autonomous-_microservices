package com.ai.sre.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * REST API representation of a self-healing action.
 */
public record HealingActionDTO(
        UUID id,
        UUID incidentId,
        String actionType,
        String targetService,
        String targetNamespace,
        String targetResource,
        String status,
        int confidenceScore,
        boolean requiresApproval,
        String approvedBy,
        Map<String, Object> beforeState,
        Map<String, Object> afterState,
        boolean dryRun,
        String executionLog,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant startedAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant completedAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant createdAt
) {}
