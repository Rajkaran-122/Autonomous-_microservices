package com.ai.sre.ai.guardrails.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "policy_rules")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PolicyRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "service_pattern")
    private String servicePattern; // e.g., "payment-.*"

    @Column(name = "action_type")
    private String actionType; // POD_RESTART, SCALE_UP, ROLLBACK, or "*"

    @Column(name = "environment")
    private String environment; // production, staging, or "*"

    @Column(name = "action_risk_level", nullable = false)
    private String actionRiskLevel; // LOW, MEDIUM, HIGH, CRITICAL

    @Column(name = "auto_execute_threshold", nullable = false)
    private Integer autoExecuteThreshold; // e.g., 95

    @Column(name = "approval_threshold", nullable = false)
    private Integer approvalThreshold; // e.g., 70

    @Column(name = "block_threshold", nullable = false)
    private Integer blockThreshold; // e.g., 0

    @Column(name = "max_actions_per_hour")
    private Integer maxActionsPerHour;

    @Column(name = "max_blast_radius")
    private Integer maxBlastRadius;

    @Column(name = "cooldown_minutes")
    private Integer cooldownMinutes;

    @Column(name = "required_approvers")
    private Integer requiredApprovers;

    @Column(name = "approval_timeout_minutes")
    private Integer approvalTimeoutMinutes;

    @Column(name = "escalation_after_minutes")
    private Integer escalationAfterMinutes;

    private Integer priority; // Lower = higher priority

    private Boolean enabled;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (enabled == null) enabled = true;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
