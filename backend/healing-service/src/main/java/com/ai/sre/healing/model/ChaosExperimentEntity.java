package com.ai.sre.healing.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "chaos_experiments")
public class ChaosExperimentEntity {

    @Id
    private UUID id;

    private String name;

    @Column(name = "target_service_id")
    private UUID targetServiceId;

    @Column(name = "target_namespace")
    private String targetNamespace;

    @Column(name = "experiment_type")
    private String experimentType;

    @JdbcTypeCode(SqlTypes.JSON)
    private String config;

    @Column(name = "cron_expression")
    private String cronExpression;

    private Boolean enabled;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public ChaosExperimentEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public UUID getTargetServiceId() { return targetServiceId; }
    public void setTargetServiceId(UUID targetServiceId) { this.targetServiceId = targetServiceId; }

    public String getTargetNamespace() { return targetNamespace; }
    public void setTargetNamespace(String targetNamespace) { this.targetNamespace = targetNamespace; }

    public String getExperimentType() { return experimentType; }
    public void setExperimentType(String experimentType) { this.experimentType = experimentType; }

    public String getConfig() { return config; }
    public void setConfig(String config) { this.config = config; }

    public String getCronExpression() { return cronExpression; }
    public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
