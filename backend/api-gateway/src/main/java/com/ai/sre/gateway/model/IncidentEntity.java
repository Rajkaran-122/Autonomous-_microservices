package com.ai.sre.gateway.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "incidents")
@Getter
@Setter
@NoArgsConstructor
public class IncidentEntity {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String title;

    private String description;
    
    @Column(nullable = false)
    private String severity;
    
    @Column(nullable = false)
    private String status;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "service_id")
    private ServiceEntity service;

    @Column(name = "detected_at")
    private Instant detectedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "root_cause")
    private String rootCause;
    
    @Column(name = "created_at")
    private Instant createdAt;
}
