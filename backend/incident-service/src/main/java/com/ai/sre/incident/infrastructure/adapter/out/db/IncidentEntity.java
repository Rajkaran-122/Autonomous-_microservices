package com.ai.sre.incident.infrastructure.adapter.out.db;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "incidents")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class IncidentEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String severity;

    @Column(nullable = false)
    private String status;

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
}
