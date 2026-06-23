package com.ai.sre.ai.guardrails.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "policy_decisions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PolicyDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "healing_action_id")
    private UUID healingActionId;

    @Column(name = "incident_id")
    private UUID incidentId;

    @Column(name = "action_type", nullable = false)
    private String actionType;

    @Column(name = "target_service")
    private String targetService;

    @Column(name = "ai_confidence_score")
    private Integer aiConfidenceScore;

    @Column(name = "action_risk_level")
    private String actionRiskLevel;

    @Column(name = "blast_radius_count")
    private Integer blastRadiusCount;

    @Column(name = "blast_radius_score")
    private Double blastRadiusScore;

    @Column(name = "combined_risk_score")
    private Double combinedRiskScore;

    @Column(nullable = false)
    private String decision; // AUTO_EXECUTE, APPROVAL_REQUIRED, BLOCKED

    @Column(name = "decision_reason", columnDefinition = "TEXT")
    private String decisionReason;

    @Column(name = "matched_policy_id")
    private UUID matchedPolicyId;

    @Column(name = "approval_status")
    private String approvalStatus; // PENDING, APPROVED, REJECTED, TIMED_OUT

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approval_requested_at")
    private Instant approvalRequestedAt;

    @Column(name = "approval_received_at")
    private Instant approvalReceivedAt;

    @Column(name = "execution_status")
    private String executionStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "execution_result", columnDefinition = "jsonb")
    private String executionResult;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
