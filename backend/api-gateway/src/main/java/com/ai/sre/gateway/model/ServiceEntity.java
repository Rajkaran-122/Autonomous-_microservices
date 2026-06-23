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
@Table(name = "services")
@Getter
@Setter
@NoArgsConstructor
public class ServiceEntity {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    private String namespace;
    
    @Column(name = "service_type")
    private String serviceType;
    
    private String tier;
    private String owner;
    
    @Column(name = "health_status")
    private String healthStatus;
    
    @Column(name = "updated_at")
    private Instant updatedAt;
}
