package com.ai.sre.healing.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "healing_actions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class HealingAction {

    @Id
    @Column(name = "id")
    private UUID id; // Using the ID from PolicyDecision

    @Column(name = "incident_id", nullable = false)
    private UUID incidentId;

    @Column(name = "action_type", nullable = false)
    private String actionType; // POD_RESTART, SCALE_UP, ROLLBACK, CANARY_DEPLOY, CHAOS_EXPERIMENT

    @Column(name = "target_service", nullable = false)
    private String targetService;

    @Column(name = "target_namespace")
    private String targetNamespace;

    @Column(name = "target_resource")
    private String targetResource;

    @Column(nullable = false)
    private String status; // PENDING_APPROVAL, QUEUED, EXECUTING, COMPLETED, FAILED, ROLLED_BACK

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String parameters;

    @Column(name = "auto_executed")
    private Boolean autoExecuted;

    @Column(name = "approved_by")
    private String approvedBy;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "pre_execution_state", columnDefinition = "jsonb")
    private String preExecutionState;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "post_execution_state", columnDefinition = "jsonb")
    private String postExecutionState;

    @Column(name = "rollback_supported")
    private Boolean rollbackSupported;

    @Column(name = "execution_logs", columnDefinition = "TEXT")
    private String executionLogs;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (status == null) status = "PENDING_APPROVAL";
    }
}
