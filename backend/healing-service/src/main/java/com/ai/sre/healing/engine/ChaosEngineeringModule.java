package com.ai.sre.healing.engine;

import com.ai.sre.common.config.KafkaTopics;
import com.ai.sre.common.event.ChaosEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Chaos Engineering module to intentionally inject faults into the system
 * to verify the SRE platform's detection and healing capabilities.
 */
@Service
public class ChaosEngineeringModule {

    private static final Logger log = LoggerFactory.getLogger(ChaosEngineeringModule.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ChaosEngineeringModule(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Injects simulated network latency to test SLO burn rate alerting
     * and auto-scaling healing actions.
     */
    public UUID injectNetworkLatency(String targetService, int addedLatencyMs, int durationMinutes) {
        UUID experimentId = UUID.randomUUID();
        log.warn("🧪 CHAOS EXPERIMENT STARTED: Injecting {}ms latency to {} for {} minutes", 
                 addedLatencyMs, targetService, durationMinutes);

        // Emit chaos event to notify monitoring systems
        ChaosEvent event = new ChaosEvent(
                UUID.randomUUID(),
                experimentId,
                UUID.randomUUID(),
                null,
                targetService,
                "Latency Injection",
                "NETWORK_LATENCY",
                "INJECTING",
                true,
                false,
                0,
                "PENDING",
                String.format("Injected %dms latency", addedLatencyMs),
                java.util.Collections.emptyMap(),
                Instant.now()
        );

        kafkaTemplate.send(KafkaTopics.CHAOS_EVENTS, targetService, event);

        // Note: In a real implementation, this would use a tool like Chaos Mesh,
        // Gremlin, or directly manipulate iptables/tc rules on the target pods.
        // For the scope of this project, we simulate the effect by publishing
        // degraded metrics or relying on a test harness in the target service.

        return experimentId;
    }

    public UUID killPod(String targetService) {
        UUID experimentId = UUID.randomUUID();
        log.warn("🧪 CHAOS EXPERIMENT STARTED: Killing random pod in {}", targetService);

        ChaosEvent event = new ChaosEvent(
                UUID.randomUUID(),
                experimentId,
                UUID.randomUUID(),
                null,
                targetService,
                "Pod Termination",
                "POD_KILL",
                "COMPLETED",
                true,
                false,
                0,
                "PENDING",
                "Pod terminated abruptly",
                java.util.Collections.emptyMap(),
                Instant.now()
        );

        kafkaTemplate.send(KafkaTopics.CHAOS_EVENTS, targetService, event);
        
        // In real execution, this would call Kubernetes API to delete a pod.
        
        return experimentId;
    }
}
