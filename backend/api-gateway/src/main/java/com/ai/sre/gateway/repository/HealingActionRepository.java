package com.ai.sre.gateway.repository;

import com.ai.sre.gateway.model.HealingActionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HealingActionRepository extends JpaRepository<HealingActionEntity, UUID> {
    long countByStatus(String status);
    
    @Query("SELECT COUNT(h) FROM HealingActionEntity h WHERE h.status = 'COMPLETED'")
    long countSuccessfulActions();
    
    Page<HealingActionEntity> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
    Page<HealingActionEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
    List<HealingActionEntity> findTop10ByOrderByCreatedAtDesc();
}
