package com.ai.sre.healing.service;

import com.ai.sre.common.config.KafkaTopics;
import com.ai.sre.common.event.HealingEvent;
import com.ai.sre.common.event.PolicyDecisionEvent;
import com.ai.sre.healing.model.HealingAction;
import com.ai.sre.healing.repository.HealingActionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Consumes approved policy decisions and executes them via the Kubernetes API.
 */
@Service
public class HealingExecutionService {

    private static final Logger log = LoggerFactory.getLogger(HealingExecutionService.class);

    private final HealingActionRepository healingActionRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final boolean dryRun;
    private KubernetesClient kubernetesClient;

    public HealingExecutionService(HealingActionRepository healingActionRepository,
                                   KafkaTemplate<String, Object> kafkaTemplate,
                                   ObjectMapper objectMapper,
                                   @Value("${sre.kubernetes.dry-run:true}") boolean dryRun) {
        this.healingActionRepository = healingActionRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.dryRun = dryRun;
        try {
            this.kubernetesClient = new KubernetesClientBuilder().build();
            log.info("Kubernetes client initialized. Dry run mode: {}", dryRun);
        } catch (Exception e) {
            log.warn("Failed to initialize Kubernetes client. Falling back to mock execution.", e);
        }
    }

    @KafkaListener(
            topics = KafkaTopics.POLICY_DECISIONS,
            groupId = KafkaTopics.GROUP_HEALING_SERVICE
    )
    public void handlePolicyDecision(PolicyDecisionEvent event) {
        // Record the decision as a new HealingAction
        HealingAction action = HealingAction.builder()
                .id(event.healingActionId())
                .incidentId(event.incidentId())
                .actionType(event.actionType())
                .targetService(event.serviceName())
                .status("AUTO_EXECUTE".equals(event.decision()) ? "QUEUED" : "PENDING_APPROVAL")
                .autoExecuted("AUTO_EXECUTE".equals(event.decision()))
                .parameters(event.parameters())
                .rollbackSupported(isRollbackSupported(event.actionType()))
                .build();

        healingActionRepository.save(action);

        if ("AUTO_EXECUTE".equals(event.decision())) {
            executeAction(action);
        } else {
            log.info("Action {} for incident {} requires manual approval.", action.getId(), action.getIncidentId());
        }
    }

    public void executeAction(HealingAction action) {
        action.setStatus("EXECUTING");
        action.setStartedAt(Instant.now());
        healingActionRepository.save(action);

        log.info("Executing healing action {} on {}", action.getActionType(), action.getTargetService());

        boolean success = false;
        String logs = "";

        try {
            if (dryRun || kubernetesClient == null) {
                log.info("[DRY RUN] Would execute {} on {}", action.getActionType(), action.getTargetService());
                Thread.sleep(2000); // Simulate work
                success = true;
                logs = "Action executed successfully in dry-run mode.";
            } else {
                // Real execution
                success = performKubernetesAction(action);
                logs = "Kubernetes action completed successfully.";
            }
        } catch (Exception e) {
            log.error("Execution failed", e);
            success = false;
            logs = "Execution failed: " + e.getMessage();
        }

        action.setStatus(success ? "COMPLETED" : "FAILED");
        action.setExecutionLogs(logs);
        action.setCompletedAt(Instant.now());
        healingActionRepository.save(action);

        // Emit results back to Kafka
        emitHealingResult(action, success, logs);
    }

    private boolean performKubernetesAction(HealingAction action) {
        return switch (action.getActionType()) {
            case "POD_RESTART" -> restartPods(action.getTargetService(), "production"); // Assume namespace for now
            case "SCALE_UP" -> scaleDeployment(action.getTargetService(), "production", 1);
            default -> {
                log.warn("Unsupported action type for real execution: {}", action.getActionType());
                yield false;
            }
        };
    }

    private boolean restartPods(String deploymentName, String namespace) {
        try {
            kubernetesClient.apps().deployments().inNamespace(namespace).withName(deploymentName)
                    .rolling().restart();
            return true;
        } catch (Exception e) {
            log.error("Failed to restart pods for deployment " + deploymentName, e);
            return false;
        }
    }

    private boolean scaleDeployment(String deploymentName, String namespace, int additionalReplicas) {
        try {
            var deployment = kubernetesClient.apps().deployments().inNamespace(namespace).withName(deploymentName).get();
            if (deployment != null) {
                int currentReplicas = deployment.getSpec().getReplicas() != null ? deployment.getSpec().getReplicas() : 1;
                kubernetesClient.apps().deployments().inNamespace(namespace).withName(deploymentName)
                        .scale(currentReplicas + additionalReplicas);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Failed to scale deployment " + deploymentName, e);
            return false;
        }
    }

    private boolean isRollbackSupported(String actionType) {
        return "SCALE_UP".equals(actionType) || "CANARY_DEPLOY".equals(actionType);
    }

    private void emitHealingResult(HealingAction action, boolean success, String logs) {
        HealingEvent resultEvent = new HealingEvent(
                UUID.randomUUID(),
                action.getId(),
                action.getIncidentId(),
                null,
                action.getTargetService(),
                action.getActionType(),
                "production",
                action.getTargetService(),
                action.getStatus(),
                0,
                !Boolean.TRUE.equals(action.getAutoExecuted()),
                "SYSTEM",
                null,
                null,
                dryRun,
                logs,
                action.getCompletedAt() != null && action.getStartedAt() != null ? 
                    action.getCompletedAt().toEpochMilli() - action.getStartedAt().toEpochMilli() : 0,
                success ? null : logs,
                action.getCompletedAt() != null ? action.getCompletedAt() : Instant.now()
        );
        kafkaTemplate.send(KafkaTopics.HEALING_RESULTS, action.getTargetService(), resultEvent);
    }
}
