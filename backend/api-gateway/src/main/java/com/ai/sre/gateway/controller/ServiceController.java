package com.ai.sre.gateway.controller;

import com.ai.sre.common.dto.ServiceDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service registry controller — manages monitored services.
 */
@RestController
@RequestMapping("/api/v1/services")
public class ServiceController {

    /**
     * GET /api/v1/services — List all registered services.
     */
    @GetMapping
    public ResponseEntity<List<ServiceDTO>> listServices() {
        // TODO: Replace with actual DB/service query
        var services = List.of(
                new ServiceDTO(UUID.randomUUID(), "payment-service", "production", "api", "critical",
                        "payments-team", 99.95, "healthy", Map.of(), Instant.now(), Instant.now()),
                new ServiceDTO(UUID.randomUUID(), "order-service", "production", "api", "critical",
                        "orders-team", 99.9, "healthy", Map.of(), Instant.now(), Instant.now()),
                new ServiceDTO(UUID.randomUUID(), "user-service", "production", "api", "high",
                        "identity-team", 99.9, "degraded", Map.of(), Instant.now(), Instant.now()),
                new ServiceDTO(UUID.randomUUID(), "inventory-service", "production", "worker", "high",
                        "supply-team", 99.5, "healthy", Map.of(), Instant.now(), Instant.now()),
                new ServiceDTO(UUID.randomUUID(), "api-gateway", "production", "gateway", "critical",
                        "platform-team", 99.99, "healthy", Map.of(), Instant.now(), Instant.now()),
                new ServiceDTO(UUID.randomUUID(), "redis-cache", "production", "cache", "high",
                        "platform-team", 99.9, "healthy", Map.of(), Instant.now(), Instant.now()),
                new ServiceDTO(UUID.randomUUID(), "postgres-primary", "production", "database", "critical",
                        "dba-team", 99.99, "healthy", Map.of(), Instant.now(), Instant.now()),
                new ServiceDTO(UUID.randomUUID(), "notification-svc", "production", "worker", "medium",
                        "platform-team", 99.0, "down", Map.of(), Instant.now(), Instant.now())
        );
        return ResponseEntity.ok(services);
    }

    /**
     * POST /api/v1/services — Register a new service.
     */
    @PostMapping
    public ResponseEntity<?> registerService(@RequestBody Map<String, Object> request) {
        // TODO: Persist to database
        return ResponseEntity.status(201).body(Map.of(
                "id", UUID.randomUUID(),
                "name", request.get("name"),
                "message", "Service registered successfully"
        ));
    }

    /**
     * GET /api/v1/services/{id}/health — Get service health status.
     */
    @GetMapping("/{id}/health")
    public ResponseEntity<?> getServiceHealth(@PathVariable UUID id) {
        return ResponseEntity.ok(Map.of(
                "serviceId", id,
                "healthStatus", "healthy",
                "lastCheckedAt", Instant.now(),
                "metrics", Map.of(
                        "errorRate", 0.02,
                        "p99LatencyMs", 145,
                        "requestsPerSecond", 350
                )
        ));
    }
}
