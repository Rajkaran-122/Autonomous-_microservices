package com.ai.sre.healing.repository;

import com.ai.sre.healing.model.HealingAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HealingActionRepository extends JpaRepository<HealingAction, UUID> {
}
