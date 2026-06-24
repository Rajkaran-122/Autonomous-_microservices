package com.ai.sre.incident.domain.model;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Pure Domain Entity representing a detected anomaly or outage event.
 * No framework (Spring/JPA) dependencies here!
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Incident {

    private UUID id;
    private String title;
    private String description;
    private String severity; // P1, P2, P3, P4
    private String status; // DETECTED, ANALYZING, ACTIVE, REMEDIATING, RESOLVED, CLOSED
    private UUID serviceId;
    private Instant detectedAt;
    private Instant resolvedAt;
    private Integer mttrSeconds;
    private String rootCause;
    private UUID aiAnalysisId;
    private String correlationKey;
    private String createdBy;
    private Instant createdAt;
    private Instant updatedAt;

    public void markAsDetected() {
        if (this.createdAt == null) this.createdAt = Instant.now();
        if (this.updatedAt == null) this.updatedAt = Instant.now();
        if (this.detectedAt == null) this.detectedAt = Instant.now();
        if (this.status == null) this.status = "DETECTED";
    }

    public void markUpdated() {
        this.updatedAt = Instant.now();
    }
}
