package com.ai.sre.ingestion.service;

import com.ai.sre.common.config.KafkaTopics;
import com.ai.sre.common.event.LogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Event Replay System — Re-injects historical log events or incidents
 * into the Kafka pipeline for time-travel debugging, simulation, or regression testing.
 */
@Service
public class EventReplayService {

    private static final Logger log = LoggerFactory.getLogger(EventReplayService.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public EventReplayService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Replays a list of past events into the pipeline.
     */
    public UUID startReplaySession(String sessionName, List<LogEvent> historicalEvents, double speedFactor) {
        UUID sessionId = UUID.randomUUID();
        log.info("Starting replay session {} ({}) with {} events at {}x speed", 
                 sessionId, sessionName, historicalEvents.size(), speedFactor);

        // In a production system, this would spawn a background job that sleeps 
        // between events according to their original timestamps divided by speedFactor.
        // For MVP, we simulate sending them sequentially.
        
        Thread.ofVirtual().start(() -> {
            try {
                for (LogEvent event : historicalEvents) {
                    // Inject a replay flag in metadata so downstream services 
                    // know not to trigger real PagerDuty alerts
                    event.metadata().put("isReplay", "true");
                    event.metadata().put("replaySessionId", sessionId.toString());

                    kafkaTemplate.send(KafkaTopics.RAW_LOG_EVENTS, event.serviceName(), event);
                    
                    if (speedFactor > 0 && speedFactor < 100) {
                        Thread.sleep((long) (1000 / speedFactor));
                    }
                }
                log.info("Replay session {} completed successfully", sessionId);
            } catch (Exception e) {
                log.error("Replay session {} failed: {}", sessionId, e.getMessage());
            }
        });

        return sessionId;
    }
}
