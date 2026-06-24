package com.ai.sre.incident.infrastructure.adapter.in.scheduler;

import com.ai.sre.incident.application.port.in.EvaluateSloUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SloEvaluationScheduler {

    private static final Logger log = LoggerFactory.getLogger(SloEvaluationScheduler.class);
    
    private final EvaluateSloUseCase evaluateSloUseCase;
    
    // In a real system, this would be fetched from a Service Registry
    private final List<String> monitoredServices = List.of("payment-service", "user-service", "inventory-service");

    public SloEvaluationScheduler(EvaluateSloUseCase evaluateSloUseCase) {
        this.evaluateSloUseCase = evaluateSloUseCase;
    }

    @Scheduled(fixedRateString = "${sre.slo.evaluation-interval:60000}")
    public void evaluateAllSlos() {
        log.debug("Starting periodic SLO evaluation for {} services", monitoredServices.size());
        for (String service : monitoredServices) {
            try {
                evaluateSloUseCase.evaluateSloForService(service);
            } catch (Exception e) {
                log.error("Failed to evaluate SLO for service {}: {}", service, e.getMessage());
            }
        }
    }
}
