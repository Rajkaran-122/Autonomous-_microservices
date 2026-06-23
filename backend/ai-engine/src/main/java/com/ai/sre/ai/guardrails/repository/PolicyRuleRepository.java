package com.ai.sre.ai.guardrails.repository;

import com.ai.sre.ai.guardrails.model.PolicyRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PolicyRuleRepository extends JpaRepository<PolicyRule, UUID> {
    
    @Query("SELECT p FROM PolicyRule p WHERE p.enabled = true ORDER BY p.priority ASC")
    List<PolicyRule> findAllEnabledSortedByPriority();
}
