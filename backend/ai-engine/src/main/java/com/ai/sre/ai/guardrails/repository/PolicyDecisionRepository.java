package com.ai.sre.ai.guardrails.repository;

import com.ai.sre.ai.guardrails.model.PolicyDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PolicyDecisionRepository extends JpaRepository<PolicyDecision, UUID> {
}
