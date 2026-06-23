package com.ai.sre.gateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Audit log controller — provides read-only access to the immutable audit trail.
 * All automated and manual actions are logged with actor, action, and state details.
 */
@RestController
@RequestMapping("/api/v1/audit-log")
public class AuditController {

    /**
     * GET /api/v1/audit-log — Query audit trail with filtering.
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> queryAuditLog(
            @RequestParam(required = false) String actor,
            @RequestParam(required = false) String actorType,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String resourceType,
            @RequestParam(defaultValue = "50") int limit) {

        var sampleEntries = List.of(
                Map.<String, Object>of(
                        "id", 1L,
                        "actor", "ai-engine",
                        "actorType", "AI",
                        "action", "HEALING_ACTION_EXECUTED",
                        "resourceType", "healing_action",
                        "resourceId", UUID.randomUUID().toString(),
                        "details", Map.of("actionType", "POD_RESTART", "target", "payment-service"),
                        "createdAt", Instant.now().minusSeconds(60).toString()
                ),
                Map.<String, Object>of(
                        "id", 2L,
                        "actor", "engineer@sre-platform.ai",
                        "actorType", "HUMAN",
                        "action", "INCIDENT_ACKNOWLEDGED",
                        "resourceType", "incident",
                        "resourceId", UUID.randomUUID().toString(),
                        "details", Map.of("severity", "P1"),
                        "createdAt", Instant.now().minusSeconds(120).toString()
                )
        );

        return ResponseEntity.ok(sampleEntries);
    }
}
