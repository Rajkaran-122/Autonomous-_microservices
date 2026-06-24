package com.ai.sre.incident.infrastructure.adapter.out.db;

import com.ai.sre.incident.application.port.out.IncidentRepositoryPort;
import com.ai.sre.incident.domain.model.Incident;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class IncidentRepositoryAdapter implements IncidentRepositoryPort {

    private final SpringDataIncidentRepository repository;

    public IncidentRepositoryAdapter(SpringDataIncidentRepository repository) {
        this.repository = repository;
    }

    @Override
    public Incident save(Incident incident) {
        if (incident.getId() == null) {
            incident.setId(UUID.randomUUID());
        }
        IncidentEntity entity = mapToEntity(incident);
        IncidentEntity savedEntity = repository.save(entity);
        return mapToDomain(savedEntity);
    }

    @Override
    public Optional<Incident> findById(UUID id) {
        return repository.findById(id).map(this::mapToDomain);
    }

    private IncidentEntity mapToEntity(Incident domain) {
        return IncidentEntity.builder()
                .id(domain.getId())
                .title(domain.getTitle())
                .description(domain.getDescription())
                .severity(domain.getSeverity())
                .status(domain.getStatus())
                .serviceId(domain.getServiceId())
                .detectedAt(domain.getDetectedAt())
                .resolvedAt(domain.getResolvedAt())
                .mttrSeconds(domain.getMttrSeconds())
                .rootCause(domain.getRootCause())
                .aiAnalysisId(domain.getAiAnalysisId())
                .correlationKey(domain.getCorrelationKey())
                .createdBy(domain.getCreatedBy())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    private Incident mapToDomain(IncidentEntity entity) {
        return Incident.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .severity(entity.getSeverity())
                .status(entity.getStatus())
                .serviceId(entity.getServiceId())
                .detectedAt(entity.getDetectedAt())
                .resolvedAt(entity.getResolvedAt())
                .mttrSeconds(entity.getMttrSeconds())
                .rootCause(entity.getRootCause())
                .aiAnalysisId(entity.getAiAnalysisId())
                .correlationKey(entity.getCorrelationKey())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
