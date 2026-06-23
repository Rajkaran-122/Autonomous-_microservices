package com.ai.sre.gateway.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "healing_actions")
@Getter
@Setter
@NoArgsConstructor
public class HealingActionEntity {
    @Id
    private UUID id;

    @Column(name = "incident_id")
    private UUID incidentId;

    @Column(name = "action_type", nullable = false)
    private String actionType;

    @Column(name = "target_service")
    private String targetService;

    @Column(nullable = false)
    private String status;

    @Column(name = "confidence_score")
    private Integer confidenceScore;

    @Column(name = "execution_log")
    private String executionLog;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "created_at")
    private Instant createdAt;
}
