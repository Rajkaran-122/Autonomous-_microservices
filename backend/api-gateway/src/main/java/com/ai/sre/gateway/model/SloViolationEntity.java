package com.ai.sre.gateway.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "slo_violations")
@Getter
@Setter
@NoArgsConstructor
public class SloViolationEntity {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "service_id")
    private ServiceEntity service;

    @Column(name = "violation_type", nullable = false)
    private String violationType;

    @Column(name = "burn_rate")
    private BigDecimal burnRate;

    @Column(name = "error_budget_remaining")
    private BigDecimal errorBudgetRemaining;

    @Column(nullable = false)
    private String severity;

    private String message;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "created_at")
    private Instant createdAt;
}
