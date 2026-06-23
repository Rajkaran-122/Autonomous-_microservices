package com.ai.sre.incident.service;

import com.ai.sre.common.config.KafkaTopics;
import com.ai.sre.common.event.AnalysisEvent;
import com.ai.sre.common.event.IncidentEvent;
import com.ai.sre.common.event.LogEvent;
import com.ai.sre.incident.model.Incident;
import com.ai.sre.incident.repository.IncidentRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Incident detection engine:
 *   1. Consumes enriched log events from Kafka
 *   2. Detects anomalies using statistical thresholds
 *   3. Deduplicates alerts using correlation keys + Redis
 *   4. Creates incident records
 *   5. Publishes incident events for AI analysis
 *
 * Uses a sliding window counter in Redis to track error rates
 * per service, firing incidents when thresholds are exceeded.
 */
@Service
public class IncidentDetectionService {

    private static final Logger log = LoggerFactory.getLogger(IncidentDetectionService.class);
    private static final int ERROR_THRESHOLD = 5;           // Errors in window to trigger incident
    private static final Duration DEDUP_WINDOW = Duration.ofMinutes(15);   // Dedup window for correlation key
    private static final Duration ERROR_WINDOW = Duration.ofMinutes(5);    // Sliding window for error counting

    private final IncidentRepository incidentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final StringRedisTemplate redisTemplate;
    private final Counter incidentCreatedCounter;

    public IncidentDetectionService(IncidentRepository incidentRepository,
                                    KafkaTemplate<String, Object> kafkaTemplate,
                                    StringRedisTemplate redisTemplate,
                                    MeterRegistry meterRegistry) {
        this.incidentRepository = incidentRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.redisTemplate = redisTemplate;
        this.incidentCreatedCounter = Counter.builder("sre.incidents.created")
                .description("Total incidents created")
                .register(meterRegistry);
    }

    /**
     * Consumes parsed log events and detects anomalies.
     */
    @KafkaListener(
            topics = KafkaTopics.PARSED_LOG_EVENTS,
            groupId = KafkaTopics.GROUP_INCIDENT_SERVICE,
            concurrency = "3"
    )
    public void detectAnomalies(LogEvent event) {
        if (!"ERROR".equals(event.logLevel()) && !"FATAL".equals(event.logLevel())) {
            return; // Only process error-level logs for incident detection
        }

        String serviceName = event.serviceName();
        String errorKey = "sre:errors:" + serviceName;
        String dedupKey = "sre:dedup:" + serviceName;

        try {
            // Increment error counter in Redis sliding window
            Long errorCount = redisTemplate.opsForValue().increment(errorKey);
            if (errorCount != null && errorCount == 1) {
                redisTemplate.expire(errorKey, ERROR_WINDOW);
            }

            // Check if threshold exceeded and not already deduplicated
            if (errorCount != null && errorCount >= ERROR_THRESHOLD) {
                Boolean alreadyFired = redisTemplate.hasKey(dedupKey);
                if (Boolean.FALSE.equals(alreadyFired)) {
                    createIncident(event, errorCount);
                    redisTemplate.opsForValue().set(dedupKey, "1", DEDUP_WINDOW);
                    redisTemplate.delete(errorKey); // Reset counter
                }
            }
        } catch (Exception e) {
            log.error("Error in anomaly detection for service={}: {}", serviceName, e.getMessage(), e);
        }
    }

    /**
     * Consumes AI analysis results and updates incidents.
     */
    @KafkaListener(
            topics = KafkaTopics.AI_ANALYSIS_RESULTS,
            groupId = KafkaTopics.GROUP_INCIDENT_SERVICE
    )
    public void handleAnalysisResult(AnalysisEvent event) {
        log.info("Received AI analysis for incident={}, confidence={}%",
                event.incidentId(), event.confidenceScore());

        incidentRepository.findById(event.incidentId()).ifPresent(incident -> {
            incident.setRootCause(event.rootCause());
            incident.setAiAnalysisId(event.analysisId());
            incident.setStatus("ACTIVE");
            incidentRepository.save(incident);

            log.info("Incident {} updated with root cause (confidence: {}%)",
                    incident.getId(), event.confidenceScore());
        });
    }

    /**
     * Creates a new incident from detected anomaly.
     */
    private void createIncident(LogEvent event, Long errorCount) {
        String correlationKey = event.serviceName() + "-error-" +
                Instant.now().toString().substring(0, 13); // Hour-level correlation

        String severity = determineSeverity(errorCount, event);

        Incident incident = Incident.builder()
                .title("High error rate on " + event.serviceName())
                .description(String.format("Error rate spike detected: %d errors in %d minutes. Latest: %s",
                        errorCount, ERROR_WINDOW.toMinutes(), truncate(event.message(), 200)))
                .severity(severity)
                .status("DETECTED")
                .detectedAt(Instant.now())
                .correlationKey(correlationKey)
                .createdBy("anomaly-detector")
                .build();

        incident = incidentRepository.save(incident);
        incidentCreatedCounter.increment();

        log.warn("🚨 Incident created: {} (severity={}, service={})",
                incident.getId(), severity, event.serviceName());

        // Publish incident event for AI analysis
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

        kafkaTemplate.send(KafkaTopics.INCIDENT_EVENTS, event.serviceName(), incidentEvent);
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
