package com.ai.sre.incident.application.service;

import com.ai.sre.common.event.IncidentEvent;
import com.ai.sre.incident.application.port.in.EvaluateSloUseCase;
import com.ai.sre.incident.application.port.out.EventPublisherPort;
import com.ai.sre.incident.application.port.out.IncidentRepositoryPort;
import com.ai.sre.incident.application.port.out.MetricsPort;
import com.ai.sre.incident.domain.model.Incident;
import com.ai.sre.incident.domain.service.BurnRateCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class SloEvaluationApplicationService implements EvaluateSloUseCase {

    private static final Logger log = LoggerFactory.getLogger(SloEvaluationApplicationService.class);

    private final MetricsPort metricsPort;
    private final IncidentRepositoryPort incidentRepositoryPort;
    private final EventPublisherPort eventPublisherPort;
    private final BurnRateCalculator burnRateCalculator;

    public SloEvaluationApplicationService(MetricsPort metricsPort,
                                           IncidentRepositoryPort incidentRepositoryPort,
                                           EventPublisherPort eventPublisherPort) {
        this.metricsPort = metricsPort;
        this.incidentRepositoryPort = incidentRepositoryPort;
        this.eventPublisherPort = eventPublisherPort;
        this.burnRateCalculator = new BurnRateCalculator();
    }

    @Override
    public void evaluateSloForService(String serviceName) {
        double sloTarget = 99.9; // Hardcoded for now, could be dynamic per service
        Duration window1h = Duration.ofHours(1);
        int budgetWindowDays = 30;

        long total1h = metricsPort.getTotalRequests(serviceName, window1h);
        long failed1h = metricsPort.getFailedRequests(serviceName, window1h);
        long good1h = total1h - failed1h;

        BurnRateCalculator.BurnRateResult result = burnRateCalculator.calculate(
                sloTarget, good1h, total1h, window1h, budgetWindowDays
        );

        if ("FAST_BURN".equals(result.alertLevel()) || "SLOW_BURN".equals(result.alertLevel())) {
            log.warn("SLO Violation detected for {}: Burn Rate = {}", serviceName, result.burnRate());
            triggerSloIncident(serviceName, result);
        }
    }

    private void triggerSloIncident(String serviceName, BurnRateCalculator.BurnRateResult result) {
        String severity = result.toSeverity();
        String correlationKey = serviceName + "-slo-" + result.alertLevel();

        Incident incident = Incident.builder()
                .title("SLO Violation: " + result.alertLevel() + " on " + serviceName)
                .description(String.format(
                        "Service %s is consuming its error budget too quickly. Burn rate: %.2f. Remaining budget: %.1f%%",
                        serviceName, result.burnRate(), result.remainingBudgetPct()))
                .severity(severity)
                .serviceId(UUID.randomUUID()) // Placeholder
                .correlationKey(correlationKey)
                .createdBy("slo-engine")
                .build();

        incident.markAsDetected();
        incident = incidentRepositoryPort.save(incident);

        IncidentEvent event = new IncidentEvent(
                UUID.randomUUID(),
                incident.getId(),
                incident.getTitle(),
                incident.getDescription(),
                severity,
                "DETECTED",
                null,
                serviceName,
                "production",
                correlationKey,
                List.of(serviceName),
                null,
                1.0 - (result.currentSli() / 100.0),
                0,
                0,
                incident.getDetectedAt(),
                Instant.now()
        );

        eventPublisherPort.publishIncidentEvent(serviceName, event);
    }
}
