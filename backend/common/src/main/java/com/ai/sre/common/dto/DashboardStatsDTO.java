package com.ai.sre.common.dto;

/**
 * Aggregated dashboard statistics for the main overview page.
 */
public record DashboardStatsDTO(
        // Active incidents
        int totalActiveIncidents,
        int p1Count,
        int p2Count,
        int p3Count,
        int p4Count,

        // Services
        int totalServices,
        int healthyServices,
        int degradedServices,
        int downServices,

        // Self-healing
        int healingActionsToday,
        int healingSuccessRate,
        int pendingApprovals,

        // SLOs
        int totalSlos,
        int slosAtRisk,
        int sloBudgetExhausted,

        // Metrics
        double avgMttrMinutes,
        double alertNoiseReductionPct,
        int automatedResolutionsPct,

        // AI
        int aiAnalysesToday,
        double avgConfidenceScore,
        double avgResponseTimeMs
) {}
