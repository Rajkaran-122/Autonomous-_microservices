package com.ai.sre.gateway.repository;

import com.ai.sre.gateway.model.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, UUID> {
    long countByHealthStatusNot(String healthStatus);
}
