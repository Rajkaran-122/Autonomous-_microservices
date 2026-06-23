package com.ai.sre.common.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Kafka event emitted when the AI engine completes analysis of an incident.
 * Published to topic: ai-analysis-results
 * Consumed by: incident-service, healing-service, notification-service
 */
public record AnalysisEvent(
        UUID eventId,
        UUID analysisId,
        UUID incidentId,
        UUID serviceId,
        String serviceName,

        // AI analysis results
        String rootCause,
        String summary,
        List<String> affectedServices,
        List<Recommendation> recommendations,
        int confidenceScore,          // 0-100

        // Model metadata
        String modelUsed,
        int tokensUsed,
        long responseTimeMs,

        // RAG context
        boolean ragUsed,
        int ragMatchCount,

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant timestamp
) {
    public AnalysisEvent {
        if (eventId == null) eventId = UUID.randomUUID();
        if (timestamp == null) timestamp = Instant.now();
    }

    /**
     * A single AI-recommended remediation action.
     */
    public record Recommendation(
            String actionType,        // POD_RESTART, SCALE_UP, ROLLBACK, CACHE_CLEAR
            String description,
            int confidenceScore,      // 0-100
            String targetResource,
            String riskLevel          // LOW, MEDIUM, HIGH, CRITICAL
    ) {}
}
