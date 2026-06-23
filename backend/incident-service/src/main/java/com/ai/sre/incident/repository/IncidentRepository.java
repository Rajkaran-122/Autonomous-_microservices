package com.ai.sre.incident.repository;

import com.ai.sre.incident.model.Incident;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, UUID> {

    Page<Incident> findByStatus(String status, Pageable pageable);
    Page<Incident> findBySeverity(String severity, Pageable pageable);
    Page<Incident> findByServiceId(UUID serviceId, Pageable pageable);

    List<Incident> findByStatusIn(List<String> statuses);

    @Query("SELECT i FROM Incident i WHERE i.status IN :statuses ORDER BY i.detectedAt DESC")
    List<Incident> findActiveIncidents(@Param("statuses") List<String> statuses);

    @Query("SELECT COUNT(i) FROM Incident i WHERE i.severity = :severity AND i.status NOT IN ('RESOLVED', 'CLOSED')")
    long countActiveBySeverity(@Param("severity") String severity);

    @Query("SELECT AVG(i.mttrSeconds) FROM Incident i WHERE i.mttrSeconds IS NOT NULL AND i.resolvedAt > :since")
    Double averageMttrSince(@Param("since") Instant since);

    boolean existsByCorrelationKeyAndStatusNotIn(String correlationKey, List<String> excludedStatuses);
}
