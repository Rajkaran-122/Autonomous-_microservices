package com.ai.sre.incident.infrastructure.adapter.out.messaging;

import com.ai.sre.common.config.KafkaTopics;
import com.ai.sre.common.event.IncidentEvent;
import com.ai.sre.incident.application.port.out.EventPublisherPort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaEventPublisherAdapter implements EventPublisherPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaEventPublisherAdapter(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishIncidentEvent(String serviceName, IncidentEvent event) {
        kafkaTemplate.send(KafkaTopics.INCIDENT_EVENTS, serviceName, event);
    }
}
