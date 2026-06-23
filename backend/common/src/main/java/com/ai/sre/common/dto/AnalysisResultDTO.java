package com.ai.sre.common.dto;

import java.util.List;
import java.util.UUID;

/**
 * AI analysis result returned to the frontend.
 */
public record AnalysisResultDTO(
        UUID id,
        UUID incidentId,
        String rootCause,
        String summary,
        List<String> affectedServices,
        List<RecommendationDTO> recommendations,
        int confidenceScore,
        String modelUsed,
        int tokensUsed,
        long responseTimeMs,
        Integer feedbackRating,
        String createdAt
) {
    public record RecommendationDTO(
            String actionType,
            String description,
            int confidenceScore,
            String targetResource,
            String riskLevel
    ) {}
}
