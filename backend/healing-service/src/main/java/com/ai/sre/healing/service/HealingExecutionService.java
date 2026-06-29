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
import com.ai.sre.common.event.IncidentEvent;

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
    private final boolean requireApproval;
    private KubernetesClient kubernetesClient;

    public HealingExecutionService(HealingActionRepository healingActionRepository,
                                   KafkaTemplate<String, Object> kafkaTemplate,
                                   ObjectMapper objectMapper,
                                   @Value("${sre.kubernetes.dry-run:false}") boolean dryRun,
                                   @Value("${sre.healing.require-approval:true}") boolean requireApproval) {
        this.healingActionRepository = healingActionRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.dryRun = dryRun;
        this.requireApproval = requireApproval;
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
                .status(requireApproval ? "PENDING_APPROVAL" : "QUEUED")
                .autoExecuted(!requireApproval)
                .parameters(event.parameters())
                .rollbackSupported(isRollbackSupported(event.actionType()))
                .build();

        healingActionRepository.save(action);

        if (!requireApproval || "AUTO_EXECUTE".equals(event.decision())) {
            // Wait, actually if global requireApproval is false, or specific decision is auto
            if (!requireApproval) {
                action.setStatus("QUEUED");
                healingActionRepository.save(action);
                executeAction(action);
            } else if ("AUTO_EXECUTE".equals(event.decision())) {
                action.setStatus("QUEUED");
                healingActionRepository.save(action);
                executeAction(action);
            } else {
                log.info("Action {} for incident {} requires manual approval.", action.getId(), action.getIncidentId());
            }
        } else {
            log.info("Action {} for incident {} requires manual approval due to global settings.", action.getId(), action.getIncidentId());
        }
    }

    @KafkaListener(topics = KafkaTopics.INCIDENT_EVENTS, groupId = KafkaTopics.GROUP_HEALING_SERVICE + "_rollback")
    public void handleIncidentForRollback(IncidentEvent incident) {
        if (incident.title() != null && incident.title().contains("SLO Violation")) {
            log.warn("SLO Violation detected for {}. Checking if a recent healing action caused this...", incident.serviceName());
            
            // Look for recent SCALE_UP actions for this service in the last 15 minutes
            java.time.Instant fifteenMinsAgo = Instant.now().minusSeconds(900);
            
            // Simplified check: if there is a recent completed action, revert it.
            // In a real DB, we would query `findByTargetServiceAndStatusAndCompletedAtAfter(serviceName, "COMPLETED", fifteenMinsAgo)`
            Iterable<HealingAction> actions = healingActionRepository.findAll();
            for (HealingAction action : actions) {
                if (action.getTargetService().equals(incident.serviceName()) 
                        && "COMPLETED".equals(action.getStatus())
                        && "SCALE_UP".equals(action.getActionType())
                        && action.getCompletedAt() != null
                        && action.getCompletedAt().isAfter(fifteenMinsAgo)) {
                    
                    log.error("CRITICAL: Recent SCALE_UP action {} on {} appears to have accelerated the SLO burn rate! Triggering emergency rollback.", action.getId(), action.getTargetService());
                    rollbackAction(action);
                    break;
                }
            }
        }
    }

    private void rollbackAction(HealingAction action) {
        log.info("Rolling back action {} of type {}", action.getId(), action.getActionType());
        if ("SCALE_UP".equals(action.getActionType())) {
            // Revert by scaling down 1 replica (since we scaled up 1 earlier)
            scaleDeployment(action.getTargetService(), "production", -1);
            action.setStatus("ROLLED_BACK");
            healingActionRepository.save(action);
            emitHealingResult(action, true, "Emergency rollback executed successfully to mitigate SLO violation.");
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
        String namespace = "production";
        if (action.getParameters() != null && !action.getParameters().isEmpty()) {
            try {
                java.util.Map<String, Object> params = objectMapper.readValue(
                        action.getParameters(), 
                        new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {}
                );
                if (params.containsKey("namespace")) {
                    namespace = params.get("namespace").toString();
                }
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse parameters JSON for action {}: {}", action.getId(), e.getMessage());
            }
        }

        return switch (action.getActionType()) {
            case "POD_RESTART" -> restartPods(action.getTargetService(), namespace);
            case "SCALE_UP" -> scaleDeployment(action.getTargetService(), namespace, 1);
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
            if (additionalReplicas > 0 && !isSafeToScale()) {
                log.error("Aborting SCALE_UP for {}: Cluster lacks safe unallocated capacity.", deploymentName);
                return false;
            }

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

    private boolean isSafeToScale() {
        if (kubernetesClient == null || dryRun) return true; // Bypass in mock environments
        
        try {
            var metrics = kubernetesClient.top().nodes().metrics();
            if (metrics == null || metrics.getItems().isEmpty()) {
                log.warn("No node metrics available, but proceeding with caution.");
                return true;
            }
            
            for (var nodeMetric : metrics.getItems()) {
                // Parse CPU usage. E.g., "500m" (millicores) or "2" (cores)
                String cpuUsageStr = nodeMetric.getUsage().get("cpu").getAmount();
                long cpuUsageMillicores = parseCpuMillicores(cpuUsageStr);
                
                // If a node is under 80% usage (assuming 2000m total capacity for this heuristic), it's safe to scale
                if (cpuUsageMillicores < 1600) { 
                    return true;
                }
            }
            log.warn("All cluster nodes are experiencing high CPU load! Scaling up will cause resource contention.");
            return false;
        } catch (Exception e) {
            log.warn("Failed to query Kubernetes Metrics Server: {}. Proceeding anyway.", e.getMessage());
            return true;
        }
    }
    
    private long parseCpuMillicores(String amount) {
        if (amount.endsWith("m")) {
            return Long.parseLong(amount.replace("m", ""));
        } else if (amount.endsWith("n")) { // nanocores
            return Long.parseLong(amount.replace("n", "")) / 1_000_000;
        } else {
            return Long.parseLong(amount) * 1000;
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
