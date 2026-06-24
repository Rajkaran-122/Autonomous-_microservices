package com.ai.sre.incident.application.port.out;

import com.ai.sre.incident.domain.model.Incident;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound Port: Interface for persisting incidents.
 * This will be implemented by the infrastructure layer (Spring Data JPA).
 */
public interface IncidentRepositoryPort {
    Incident save(Incident incident);
    Optional<Incident> findById(UUID id);
}
