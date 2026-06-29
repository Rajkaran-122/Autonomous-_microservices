package com.ai.sre.incident.application.service;

import com.ai.sre.common.event.IncidentEvent;
import com.ai.sre.common.event.LogEvent;
import com.ai.sre.incident.application.port.in.DetectAnomalyUseCase;
import com.ai.sre.incident.application.port.out.AnomalyStatePort;
import com.ai.sre.incident.application.port.out.EventPublisherPort;
import com.ai.sre.incident.application.port.out.IncidentRepositoryPort;
import com.ai.sre.incident.domain.model.Incident;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class IncidentDetectionApplicationService implements DetectAnomalyUseCase {

    private static final Logger log = LoggerFactory.getLogger(IncidentDetectionApplicationService.class);
    
    private static final int Z_SCORE_THRESHOLD = 3;
    private static final int MIN_SAMPLES = 5;
    private static final int HISTORY_MINUTES = 60;
    private static final Duration DEDUP_WINDOW = Duration.ofMinutes(15);

    private final IncidentRepositoryPort repositoryPort;
    private final AnomalyStatePort anomalyStatePort;
    private final EventPublisherPort eventPublisherPort;

    public IncidentDetectionApplicationService(IncidentRepositoryPort repositoryPort,
                                               AnomalyStatePort anomalyStatePort,
                                               EventPublisherPort eventPublisherPort) {
        this.repositoryPort = repositoryPort;
        this.anomalyStatePort = anomalyStatePort;
        this.eventPublisherPort = eventPublisherPort;
    }

    @Override
    public void detectAnomaly(LogEvent event) {
        if (!"ERROR".equals(event.logLevel()) && !"FATAL".equals(event.logLevel())) {
            return;
        }

        String serviceName = event.serviceName();

        try {
            long currentMinute = Instant.now().getEpochSecond() / 60;
            
            // 1. Record this error in the current minute bucket
            anomalyStatePort.recordErrorMinute(serviceName, currentMinute);
            
            // 2. We still increment the rolling counter to know how many happened recently
            Long currentWindowErrors = anomalyStatePort.incrementErrorCount(serviceName, Duration.ofMinutes(1));

            // 3. Fetch historical baseline
            List<Long> history = anomalyStatePort.getHistoricalErrorCounts(serviceName, currentMinute, HISTORY_MINUTES);
            
            // Need a minimum baseline to compute standard deviation
            long nonZeroBuckets = history.stream().filter(c -> c > 0).count();
            
            if (nonZeroBuckets >= MIN_SAMPLES && currentWindowErrors != null) {
                double mean = history.stream().mapToDouble(Long::doubleValue).average().orElse(0.0);
                double variance = history.stream().mapToDouble(val -> Math.pow(val - mean, 2)).average().orElse(0.0);
                double stdDev = Math.sqrt(variance);
                
                // Avoid division by zero if stdDev is 0
                double zScore = (stdDev > 0) ? (currentWindowErrors - mean) / stdDev : 0;
                
                log.debug("Service {} - Mean: {}, StdDev: {}, Current: {}, Z-Score: {}", serviceName, mean, stdDev, currentWindowErrors, zScore);

                if (zScore >= Z_SCORE_THRESHOLD) {
                    if (anomalyStatePort.tryAcquireDedupLock(serviceName, DEDUP_WINDOW)) {
                        createIncident(event, currentWindowErrors, zScore);
                    }
                }
            } else if (currentWindowErrors != null && currentWindowErrors > 50) {
                // Fallback for massive instant spikes before baseline is established
                if (anomalyStatePort.tryAcquireDedupLock(serviceName, DEDUP_WINDOW)) {
                    createIncident(event, currentWindowErrors, 99.0);
                }
            }
            
        } catch (Exception e) {
            log.error("Error in anomaly detection for service={}: {}", serviceName, e.getMessage(), e);
        }
    }

    private void createIncident(LogEvent event, Long errorCount, double zScore) {
        String correlationKey = event.serviceName() + "-error-" +
                Instant.now().toString().substring(0, 13);

        String severity = determineSeverity(errorCount, event);

        Incident incident = Incident.builder()
                .title("Anomalous error rate on " + event.serviceName())
                .description(String.format("Statistical anomaly detected. Errors: %d/min. Z-Score: %.2f. Latest log: %s",
                        errorCount, zScore, truncate(event.message(), 200)))
                .severity(severity)
                .correlationKey(correlationKey)
                .createdBy("anomaly-detector")
                .serviceId(UUID.randomUUID()) // placeholder
                .build();

        incident.markAsDetected();
        incident = repositoryPort.save(incident);

        log.warn("🚨 Incident created: {} (severity={}, service={}, zScore={})",
                incident.getId(), severity, event.serviceName(), zScore);

        IncidentEvent incidentEvent = new IncidentEvent(
                UUID.randomUUID(),
                incident.getId(),
                incident.getTitle(),
                incident.getDescription(),
                severity,
                "DETECTED",
                null,
                event.serviceName(),
                event.namespace(),
                correlationKey,
                List.of(event.serviceName()),
                null,
                errorCount.doubleValue(),
                0,
                0,
                incident.getDetectedAt(),
                Instant.now()
        );

        eventPublisherPort.publishIncidentEvent(event.serviceName(), incidentEvent);
    }

    private String determineSeverity(Long errorCount, LogEvent event) {
        if ("FATAL".equals(event.logLevel()) || errorCount > 50) return "P1";
        if (errorCount > 20) return "P2";
        if (errorCount > 10) return "P3";
        return "P4";
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        return str.length() <= maxLength ? str : str.substring(0, maxLength) + "...";
    }
}
