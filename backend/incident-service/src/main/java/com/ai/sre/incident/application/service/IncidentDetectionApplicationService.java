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
    
    private static final int ERROR_THRESHOLD = 5;
    private static final Duration ERROR_WINDOW = Duration.ofMinutes(5);
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
            Long errorCount = anomalyStatePort.incrementErrorCount(serviceName, ERROR_WINDOW);

            if (errorCount != null && errorCount >= ERROR_THRESHOLD) {
                if (!anomalyStatePort.isAlreadyDeduplicated(serviceName)) {
                    createIncident(event, errorCount);
                    anomalyStatePort.markAsDeduplicated(serviceName, DEDUP_WINDOW);
                    anomalyStatePort.resetErrorCount(serviceName);
                }
            }
        } catch (Exception e) {
            log.error("Error in anomaly detection for service={}: {}", serviceName, e.getMessage(), e);
        }
    }

    private void createIncident(LogEvent event, Long errorCount) {
        String correlationKey = event.serviceName() + "-error-" +
                Instant.now().toString().substring(0, 13);

        String severity = determineSeverity(errorCount, event);

        Incident incident = Incident.builder()
                .title("High error rate on " + event.serviceName())
                .description(String.format("Error rate spike detected: %d errors in %d minutes. Latest: %s",
                        errorCount, ERROR_WINDOW.toMinutes(), truncate(event.message(), 200)))
                .severity(severity)
                .correlationKey(correlationKey)
                .createdBy("anomaly-detector")
                .serviceId(UUID.randomUUID()) // placeholder
                .build();

        incident.markAsDetected();
        incident = repositoryPort.save(incident);

        log.warn("🚨 Incident created: {} (severity={}, service={})",
                incident.getId(), severity, event.serviceName());

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
