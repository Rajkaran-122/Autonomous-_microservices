package com.ai.sre.incident.infrastructure.adapter.out.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpringDataIncidentRepository extends JpaRepository<IncidentEntity, UUID> {
}
