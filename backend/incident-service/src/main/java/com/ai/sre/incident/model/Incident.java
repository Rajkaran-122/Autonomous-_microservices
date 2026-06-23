package com.ai.sre.incident.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Incident entity — represents a detected anomaly or outage event.
 */
@Entity
@Table(name = "incidents")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String severity; // P1, P2, P3, P4

    @Column(nullable = false)
    private String status; // DETECTED, ANALYZING, ACTIVE, REMEDIATING, RESOLVED, CLOSED

    @Column(name = "service_id")
    private UUID serviceId;

    @Column(name = "detected_at", nullable = false)
    private Instant detectedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "mttr_seconds")
    private Integer mttrSeconds;

    @Column(name = "root_cause", columnDefinition = "TEXT")
    private String rootCause;

    @Column(name = "ai_analysis_id")
    private UUID aiAnalysisId;

    @Column(name = "correlation_key")
    private String correlationKey;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = Instant.now();
        if (detectedAt == null) detectedAt = Instant.now();
        if (status == null) status = "DETECTED";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
