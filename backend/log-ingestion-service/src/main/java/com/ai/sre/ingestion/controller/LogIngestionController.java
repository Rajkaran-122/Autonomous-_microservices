package com.ai.sre.ingestion.controller;

import com.ai.sre.common.config.KafkaTopics;
import com.ai.sre.common.event.LogEvent;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST API for log ingestion — allows services to push logs via HTTP
 * as an alternative to direct Kafka production.
 */
@RestController
@RequestMapping("/api/v1/logs")
public class LogIngestionController {

    private static final Logger log = LoggerFactory.getLogger(LogIngestionController.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public LogIngestionController(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * POST /api/v1/logs — Ingest a single log entry.
     */
    @PostMapping
    public ResponseEntity<?> ingestLog(@Valid @RequestBody LogIngestRequest request) {
        LogEvent event = new LogEvent(
                UUID.randomUUID(),
                request.serviceName(),
                request.namespace(),
                request.logLevel(),
                request.message(),
                request.rawPayload(),
                request.logFormat(),
                request.traceId(),
                request.spanId(),
                request.metadata(),
                request.timestamp() != null ? request.timestamp() : Instant.now()
        );

        kafkaTemplate.send(KafkaTopics.RAW_LOG_EVENTS, request.serviceName(), event);
        log.debug("Log ingested for service={}", request.serviceName());

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(Map.of("eventId", event.eventId(), "status", "ACCEPTED"));
    }

    /**
     * POST /api/v1/logs/batch — Ingest multiple log entries.
     */
    @PostMapping("/batch")
    public ResponseEntity<?> ingestBatch(@RequestBody List<LogIngestRequest> requests) {
        int accepted = 0;
        for (LogIngestRequest request : requests) {
            try {
                LogEvent event = new LogEvent(
                        UUID.randomUUID(), request.serviceName(), request.namespace(),
                        request.logLevel(), request.message(), request.rawPayload(),
                        request.logFormat(), request.traceId(), request.spanId(),
                        request.metadata(), request.timestamp() != null ? request.timestamp() : Instant.now()
                );
                kafkaTemplate.send(KafkaTopics.RAW_LOG_EVENTS, request.serviceName(), event);
                accepted++;
            } catch (Exception e) {
                log.warn("Failed to ingest batch entry: {}", e.getMessage());
            }
        }
        return ResponseEntity.ok(Map.of("accepted", accepted, "total", requests.size()));
    }

    public record LogIngestRequest(
            @NotBlank String serviceName,
            String namespace,
            String logLevel,
            @NotBlank String message,
            String rawPayload,
            String logFormat,
            String traceId,
            String spanId,
            Map<String, String> metadata,
            Instant timestamp
    ) {}
}
