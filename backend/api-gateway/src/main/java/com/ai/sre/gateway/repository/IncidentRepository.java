package com.ai.sre.gateway.repository;

import com.ai.sre.gateway.model.IncidentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IncidentRepository extends JpaRepository<IncidentEntity, UUID> {
    long countByStatus(String status);
    long countBySeverityAndStatus(String severity, String status);
    Page<IncidentEntity> findAllByOrderByDetectedAtDesc(Pageable pageable);
    List<IncidentEntity> findTop5ByOrderByDetectedAtDesc();
}
