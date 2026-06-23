package com.ai.sre.gateway.controller;

import com.ai.sre.gateway.repository.HealingActionRepository;
import com.ai.sre.gateway.repository.IncidentRepository;
import com.ai.sre.gateway.repository.ServiceRepository;
import com.ai.sre.gateway.repository.SloViolationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final IncidentRepository incidentRepository;
    private final ServiceRepository serviceRepository;
    private final HealingActionRepository healingActionRepository;
    private final SloViolationRepository sloViolationRepository;

    public DashboardController(IncidentRepository incidentRepository,
                               ServiceRepository serviceRepository,
                               HealingActionRepository healingActionRepository,
                               SloViolationRepository sloViolationRepository) {
        this.incidentRepository = incidentRepository;
        this.serviceRepository = serviceRepository;
        this.healingActionRepository = healingActionRepository;
        this.sloViolationRepository = sloViolationRepository;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalServices = serviceRepository.count();
        long unhealthyServices = serviceRepository.countByHealthStatusNot("healthy");
        
        long activeIncidents = incidentRepository.countByStatus("ACTIVE");
        long criticalIncidents = incidentRepository.countBySeverityAndStatus("P1", "ACTIVE");
        
        long totalHealing = healingActionRepository.count();
        long successfulHealing = healingActionRepository.countSuccessfulActions();
        
        long sloViolations = sloViolationRepository.countByResolvedAtIsNull();
        
        stats.put("servicesMonitored", totalServices);
        stats.put("unhealthyServices", unhealthyServices);
        stats.put("activeIncidents", activeIncidents);
        stats.put("criticalIncidents", criticalIncidents);
        stats.put("healingActions", totalHealing);
        stats.put("healingSuccessRate", totalHealing > 0 ? (int)((successfulHealing * 100.0) / totalHealing) : 100);
        stats.put("sloViolations", sloViolations);
        stats.put("aiAnalyses", totalHealing + 5); // Simulated relative metric for AI engine runs
        
        return ResponseEntity.ok(stats);
    }
}
