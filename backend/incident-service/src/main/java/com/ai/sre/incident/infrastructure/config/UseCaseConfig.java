package com.ai.sre.incident.infrastructure.config;

import com.ai.sre.incident.application.port.out.AnomalyStatePort;
import com.ai.sre.incident.application.port.out.EventPublisherPort;
import com.ai.sre.incident.application.port.out.IncidentRepositoryPort;
import com.ai.sre.incident.application.port.out.MetricsPort;
import com.ai.sre.incident.application.port.in.EvaluateSloUseCase;
import com.ai.sre.incident.application.service.IncidentDetectionApplicationService;
import com.ai.sre.incident.application.service.SloEvaluationApplicationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public IncidentDetectionApplicationService incidentDetectionApplicationService(
            IncidentRepositoryPort repositoryPort,
            AnomalyStatePort anomalyStatePort,
            EventPublisherPort eventPublisherPort) {
        
        return new IncidentDetectionApplicationService(
                repositoryPort, 
                anomalyStatePort, 
                eventPublisherPort
        );
    }
    @Bean
    public EvaluateSloUseCase evaluateSloUseCase(MetricsPort metricsPort,
                                                 IncidentRepositoryPort incidentRepositoryPort,
                                                 EventPublisherPort eventPublisherPort) {
        return new SloEvaluationApplicationService(metricsPort, incidentRepositoryPort, eventPublisherPort);
    }
}
