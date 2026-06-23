package com.ai.sre.ingestion.service;

import com.ai.sre.common.config.KafkaTopics;
import com.ai.sre.common.event.LogEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Core log processing pipeline:
 *   1. Consumes raw log events from Kafka
 *   2. Detects log format (JSON, logfmt, plaintext)
 *   3. Parses and extracts structured fields
 *   4. Enriches with service metadata
 *   5. Computes anomaly score
 *   6. Publishes parsed events for downstream processing
 *
 * Hexagonal Architecture: This is a domain service — no framework
 * dependencies in the core logic (parsing, scoring).
 */
@Service
public class LogProcessingService {

    private static final Logger log = LoggerFactory.getLogger(LogProcessingService.class);
    private static final Pattern LOGFMT_PATTERN = Pattern.compile("(\\w+)=(\"[^\"]*\"|\\S+)");
    private static final Pattern ERROR_PATTERN = Pattern.compile(
            "(?i)(exception|error|fatal|panic|crash|oom|out.of.memory|timeout|connection.refused|ECONNREFUSED)"
    );

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final Counter processedCounter;
    private final Counter errorCounter;
    private final Timer processingTimer;

    public LogProcessingService(KafkaTemplate<String, Object> kafkaTemplate,
                                ObjectMapper objectMapper,
                                MeterRegistry meterRegistry) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;

        this.processedCounter = Counter.builder("sre.logs.processed")
                .description("Total logs processed")
                .register(meterRegistry);
        this.errorCounter = Counter.builder("sre.logs.errors")
                .description("Log processing errors")
                .register(meterRegistry);
        this.processingTimer = Timer.builder("sre.logs.processing.time")
                .description("Log processing duration")
                .register(meterRegistry);
    }

    /**
     * Main Kafka consumer — processes incoming raw log events.
     */
    @KafkaListener(
            topics = KafkaTopics.RAW_LOG_EVENTS,
            groupId = KafkaTopics.GROUP_LOG_INGESTION,
            concurrency = "3"
    )
    public void processLogEvent(LogEvent event) {
        processingTimer.record(() -> {
            try {
                log.debug("Processing log from service={}, level={}", event.serviceName(), event.logLevel());

                // Step 1: Detect format and parse
                Map<String, Object> parsedFields = parseLogMessage(event.message(), event.logFormat());

                // Step 2: Extract severity from content if not provided
                String effectiveLevel = event.logLevel();
                if (effectiveLevel == null || effectiveLevel.isBlank()) {
                    effectiveLevel = detectLogLevel(event.message());
                }

                // Step 3: Compute anomaly score
                int anomalyScore = computeAnomalyScore(event.message(), effectiveLevel, parsedFields);

                // Step 4: Build enriched metadata
                Map<String, String> enrichedMetadata = new HashMap<>();
                if (event.metadata() != null) enrichedMetadata.putAll(event.metadata());
                enrichedMetadata.put("anomalyScore", String.valueOf(anomalyScore));
                enrichedMetadata.put("parsedFormat", detectFormat(event.message()));
                enrichedMetadata.put("processingTimestamp", Instant.now().toString());

                // Step 5: Create enriched event
                LogEvent enrichedEvent = new LogEvent(
                        event.eventId(),
                        event.serviceName(),
                        event.namespace(),
                        effectiveLevel,
                        event.message(),
                        event.rawPayload(),
                        detectFormat(event.message()),
                        event.traceId(),
                        event.spanId(),
                        enrichedMetadata,
                        event.timestamp()
                );

                // Step 6: Publish to parsed-log-events topic
                kafkaTemplate.send(KafkaTopics.PARSED_LOG_EVENTS, event.serviceName(), enrichedEvent);

                processedCounter.increment();

                // Step 7: If anomaly score is high, fast-track to incident detection
                if (anomalyScore >= 80) {
                    log.warn("High anomaly score ({}) for service={}: {}", anomalyScore, event.serviceName(), event.message());
                    kafkaTemplate.send(KafkaTopics.INCIDENT_EVENTS, event.serviceName(), enrichedEvent);
                }

            } catch (Exception e) {
                log.error("Failed to process log event: {}", e.getMessage(), e);
                errorCounter.increment();
                // Send to DLQ
                kafkaTemplate.send(KafkaTopics.DLQ_RAW_LOG_EVENTS, event.serviceName(), event);
            }
        });
    }

    /**
     * Parses a log message based on its format.
     */
    Map<String, Object> parseLogMessage(String message, String format) {
        if (message == null || message.isBlank()) return Map.of();

        return switch (detectFormat(message)) {
            case "JSON" -> parseJson(message);
            case "LOGFMT" -> parseLogfmt(message);
            default -> parsePlaintext(message);
        };
    }

    /**
     * Detects the format of a log message.
     */
    String detectFormat(String message) {
        if (message == null) return "PLAINTEXT";
        String trimmed = message.trim();
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) return "JSON";
        if (LOGFMT_PATTERN.matcher(trimmed).find()) return "LOGFMT";
        return "PLAINTEXT";
    }

    /**
     * Parses JSON log messages.
     */
    Map<String, Object> parseJson(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            Map<String, Object> fields = new HashMap<>();
            node.fields().forEachRemaining(entry -> fields.put(entry.getKey(), entry.getValue().asText()));
            return fields;
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse JSON log: {}", e.getMessage());
            return Map.of("raw", message);
        }
    }

    /**
     * Parses logfmt-style messages (key=value pairs).
     */
    Map<String, Object> parseLogfmt(String message) {
        Map<String, Object> fields = new HashMap<>();
        Matcher matcher = LOGFMT_PATTERN.matcher(message);
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2).replaceAll("^\"|\"$", "");
            fields.put(key, value);
        }
        return fields;
    }

    /**
     * Extracts basic fields from plaintext log messages.
     */
    Map<String, Object> parsePlaintext(String message) {
        return Map.of("raw", message, "wordCount", message.split("\\s+").length);
    }

    /**
     * Detects log level from message content.
     */
    String detectLogLevel(String message) {
        if (message == null) return "INFO";
        String upper = message.toUpperCase();
        if (upper.contains("FATAL") || upper.contains("PANIC")) return "FATAL";
        if (upper.contains("ERROR") || upper.contains("EXCEPTION")) return "ERROR";
        if (upper.contains("WARN")) return "WARN";
        if (upper.contains("DEBUG")) return "DEBUG";
        return "INFO";
    }

    /**
     * Computes an anomaly score (0-100) based on log content analysis.
     * Higher scores indicate more suspicious/abnormal log entries.
     */
    int computeAnomalyScore(String message, String logLevel, Map<String, Object> parsedFields) {
        int score = 0;

        // Log level weighting
        score += switch (logLevel) {
            case "FATAL" -> 50;
            case "ERROR" -> 35;
            case "WARN" -> 15;
            default -> 0;
        };

        // Error pattern matching
        if (message != null) {
            Matcher matcher = ERROR_PATTERN.matcher(message);
            int matchCount = 0;
            while (matcher.find()) matchCount++;
            score += Math.min(matchCount * 10, 30);

            // Stack trace detection
            if (message.contains("at ") && message.contains(".java:")) {
                score += 15;
            }

            // OOM / resource exhaustion
            if (message.toUpperCase().contains("OUT OF MEMORY") || message.toUpperCase().contains("OOM")) {
                score += 20;
            }
        }

        return Math.min(score, 100);
    }
}
