package com.ai.sre.gateway.repository;

import com.ai.sre.gateway.model.SloViolationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SloViolationRepository extends JpaRepository<SloViolationEntity, UUID> {
    long countByResolvedAtIsNull();
    Page<SloViolationEntity> findAllByOrderByStartedAtDesc(Pageable pageable);
}
