package com.ai.sre.ai.guardrails;

import com.ai.sre.ai.guardrails.model.PolicyDecision;
import com.ai.sre.ai.guardrails.model.PolicyRule;
import com.ai.sre.ai.guardrails.repository.PolicyDecisionRepository;
import com.ai.sre.ai.guardrails.repository.PolicyRuleRepository;
import com.ai.sre.common.config.KafkaTopics;
import com.ai.sre.common.event.AnalysisEvent;
import com.ai.sre.common.event.PolicyDecisionEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Evaluates AI recommendations against defined security and operational policies.
 * Emits PolicyDecisionEvents for either auto-execution or manual approval.
 */
@Service
public class PolicyEngineService {

    private static final Logger log = LoggerFactory.getLogger(PolicyEngineService.class);

    private final PolicyRuleRepository policyRuleRepository;
    private final PolicyDecisionRepository policyDecisionRepository;
    private final RiskMatrixScorer riskMatrixScorer;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public PolicyEngineService(PolicyRuleRepository policyRuleRepository,
                               PolicyDecisionRepository policyDecisionRepository,
                               RiskMatrixScorer riskMatrixScorer,
                               KafkaTemplate<String, Object> kafkaTemplate,
                               ObjectMapper objectMapper) {
        this.policyRuleRepository = policyRuleRepository;
        this.policyDecisionRepository = policyDecisionRepository;
        this.riskMatrixScorer = riskMatrixScorer;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Evaluates a list of recommendations from the AI engine.
     */
    public void evaluateRecommendations(AnalysisEvent event) {
        if (event.recommendations() == null || event.recommendations().isEmpty()) {
            return;
        }

        List<PolicyRule> activePolicies = policyRuleRepository.findAllEnabledSortedByPriority();
        
        // Use a default fallback policy if none match
        PolicyRule defaultPolicy = PolicyRule.builder()
                .name("Default Fail-Safe")
                .actionRiskLevel("HIGH")
                .autoExecuteThreshold(100) // Never auto-execute
                .approvalThreshold(50)
                .maxBlastRadius(1)
                .build();

        for (AnalysisEvent.Recommendation rec : event.recommendations()) {
            PolicyRule matchedPolicy = findMatchingPolicy(rec, activePolicies, defaultPolicy);
            int blastRadius = event.affectedServices() != null ? event.affectedServices().size() : 0;

            RiskMatrixScorer.RiskAssessment assessment = riskMatrixScorer.assess(rec, matchedPolicy, blastRadius);

            UUID healingActionId = UUID.randomUUID();

            PolicyDecision decisionRecord = PolicyDecision.builder()
                    .healingActionId(healingActionId)
                    .incidentId(event.incidentId())
                    .actionType(rec.actionType())
                    .targetService(rec.target())
                    .aiConfidenceScore(rec.confidenceScore())
                    .actionRiskLevel(matchedPolicy.getActionRiskLevel())
                    .blastRadiusCount(blastRadius)
                    .blastRadiusScore((double) assessment.blastRadiusScore())
                    .combinedRiskScore(assessment.combinedScore())
                    .decision(assessment.decision())
                    .decisionReason(assessment.reason())
                    .matchedPolicyId(matchedPolicy.getId())
                    .approvalStatus("AUTO_EXECUTE".equals(assessment.decision()) ? "APPROVED" : 
                                   "APPROVAL_REQUIRED".equals(assessment.decision()) ? "PENDING" : "REJECTED")
                    .build();

            policyDecisionRepository.save(decisionRecord);
            log.info("Policy Engine Decision: {} for {} on {}. Reason: {}", 
                    assessment.decision(), rec.actionType(), rec.target(), assessment.reason());

            // Emit the decision to Kafka
            emitDecisionEvent(event.incidentId(), healingActionId, rec, decisionRecord);
        }
    }

    private PolicyRule findMatchingPolicy(AnalysisEvent.Recommendation rec, List<PolicyRule> policies, PolicyRule defaultPolicy) {
        for (PolicyRule policy : policies) {
            boolean matchService = policy.getServicePattern() == null || 
                                 policy.getServicePattern().equals("*") ||
                                 Pattern.compile(policy.getServicePattern()).matcher(rec.target()).matches();
            
            boolean matchAction = policy.getActionType() == null ||
                                policy.getActionType().equals("*") ||
                                policy.getActionType().equalsIgnoreCase(rec.actionType());
                                
            if (matchService && matchAction) {
                return policy;
            }
        }
        return defaultPolicy;
    }

    private void emitDecisionEvent(UUID incidentId, UUID healingActionId, AnalysisEvent.Recommendation rec, PolicyDecision decision) {
        try {
            PolicyDecisionEvent event = new PolicyDecisionEvent(
                    UUID.randomUUID(),
                    incidentId,
                    healingActionId,
                    rec.actionType(),
                    rec.target(),
                    decision.getDecision(),
                    decision.getDecisionReason(),
                    decision.getCombinedRiskScore(),
                    decision.getApprovalStatus(),
                    objectMapper.writeValueAsString(rec.parameters()),
                    decision.getCreatedAt()
            );

            kafkaTemplate.send(KafkaTopics.POLICY_DECISIONS, rec.target(), event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize parameters for policy decision event", e);
        }
    }
}
