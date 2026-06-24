package com.ai.sre.ai.service.telemetry;

public interface TelemetryContextGatherer {
    
    /**
     * Gathers recent stack traces and logs for the given service.
     */
    String gatherStackTraces(String serviceName);

    /**
     * Gathers recent commit messages or deployment info for the given service.
     */
    String gatherRecentCommits(String serviceName);

    /**
     * Gathers recent critical metric anomalies (e.g., CPU spikes) for the given service.
     */
    String gatherMetrics(String serviceName);
}
