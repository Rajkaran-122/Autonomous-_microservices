package com.ai.sre.incident.infrastructure.adapter.in.messaging;

import com.ai.sre.common.config.KafkaTopics;
import com.ai.sre.common.event.LogEvent;
import com.ai.sre.incident.application.port.in.DetectAnomalyUseCase;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaIncidentListener {

    private final DetectAnomalyUseCase detectAnomalyUseCase;

    public KafkaIncidentListener(DetectAnomalyUseCase detectAnomalyUseCase) {
        this.detectAnomalyUseCase = detectAnomalyUseCase;
    }

    @KafkaListener(
            topics = KafkaTopics.PARSED_LOG_EVENTS,
            groupId = KafkaTopics.GROUP_INCIDENT_SERVICE,
            concurrency = "3"
    )
    public void detectAnomalies(LogEvent event) {
        detectAnomalyUseCase.detectAnomaly(event);
    }
}
