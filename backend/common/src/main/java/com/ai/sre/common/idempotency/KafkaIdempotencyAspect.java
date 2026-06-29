package com.ai.sre.common.idempotency;

import jakarta.annotation.PostConstruct;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

/**
 * Universal Idempotency Guard for all Kafka consumers.
 * Intercepts any @KafkaListener method, extracts the event's unique ID,
 * and ensures it hasn't been processed by this consumer group yet via a PostgreSQL unique constraint.
 */
@Aspect
@Component
@Order(1) // Run before business logic
public class KafkaIdempotencyAspect {

    private static final Logger log = LoggerFactory.getLogger(KafkaIdempotencyAspect.class);
    private final JdbcTemplate jdbcTemplate;

    public KafkaIdempotencyAspect(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        try {
            jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS idempotent_events (" +
                "event_id VARCHAR(255) PRIMARY KEY, " +
                "consumer_group VARCHAR(255), " +
                "processed_at TIMESTAMP" +
                ")"
            );
            log.info("Idempotency guard initialized: table idempotent_events ensured.");
        } catch (Exception e) {
            log.warn("Failed to ensure idempotent_events table exists. System will proceed without strict idempotency if DB is unavailable.", e);
        }
    }

    @Around("@annotation(kafkaListener)")
    public Object checkIdempotency(ProceedingJoinPoint joinPoint, KafkaListener kafkaListener) throws Throwable {
        Object[] args = joinPoint.getArgs();
        if (args.length == 0 || args[0] == null) {
            return joinPoint.proceed();
        }

        Object event = args[0];
        String eventIdStr = extractEventId(event);
        
        if (eventIdStr == null) {
            log.warn("Could not extract eventId from {}, skipping idempotency check.", event.getClass().getSimpleName());
            return joinPoint.proceed();
        }

        String groupId = getGroupId(kafkaListener);
        String uniqueId = eventIdStr + "-" + groupId;

        try {
            // Attempt to insert the record atomically. 
            // If it succeeds, we are the first to process it.
            jdbcTemplate.update(
                "INSERT INTO idempotent_events (event_id, consumer_group, processed_at) VALUES (?, ?, ?)",
                uniqueId, groupId, Timestamp.from(Instant.now())
            );
        } catch (DataIntegrityViolationException e) {
            log.info("Idempotency Guard Active: Event {} was already processed by {}. Skipping.", eventIdStr, groupId);
            return null; // Skip execution completely
        } catch (Exception e) {
            log.warn("Idempotency check failed (DB issue?). Proceeding to prevent blockage: {}", e.getMessage());
        }

        return joinPoint.proceed();
    }

    private String getGroupId(KafkaListener listener) {
        if (listener.groupId() != null && !listener.groupId().isEmpty()) {
            return listener.groupId();
        }
        if (listener.id() != null && !listener.id().isEmpty()) {
            return listener.id();
        }
        return "default-group";
    }

    private String extractEventId(Object event) {
        try {
            // Check for eventId()
            Method method = event.getClass().getMethod("eventId");
            Object id = method.invoke(event);
            if (id instanceof UUID) return id.toString();
            if (id instanceof String) return (String) id;
        } catch (Exception e) {
            try {
                // Check for id()
                Method method = event.getClass().getMethod("id");
                Object id = method.invoke(event);
                if (id != null) return id.toString();
            } catch (Exception ex) {
                // Ignore
            }
        }
        return null;
    }
}
