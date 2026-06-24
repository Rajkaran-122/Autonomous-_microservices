package com.ai.sre.healing.repository;

import com.ai.sre.healing.model.ChaosExperimentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChaosExperimentRepository extends JpaRepository<ChaosExperimentEntity, UUID> {
    
    // Find all experiments that are enabled and have a cron expression set
    List<ChaosExperimentEntity> findByEnabledTrueAndCronExpressionIsNotNull();
}
