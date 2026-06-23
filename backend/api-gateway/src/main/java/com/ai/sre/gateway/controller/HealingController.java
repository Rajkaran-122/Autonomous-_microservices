package com.ai.sre.gateway.controller;

import com.ai.sre.gateway.model.HealingActionEntity;
import com.ai.sre.gateway.repository.HealingActionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/healing")
public class HealingController {

    private final HealingActionRepository healingActionRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public HealingController(HealingActionRepository healingActionRepository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.healingActionRepository = healingActionRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @GetMapping
    public ResponseEntity<Page<HealingActionEntity>> listActions(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(healingActionRepository.findAllByOrderByCreatedAtDesc(pageable));
    }
    
    @GetMapping("/history")
    public ResponseEntity<List<HealingActionEntity>> getRecentHistory() {
        return ResponseEntity.ok(healingActionRepository.findTop10ByOrderByCreatedAtDesc());
    }

    @GetMapping("/pending")
    public ResponseEntity<Page<HealingActionEntity>> listPendingApprovals(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(healingActionRepository.findByStatusOrderByCreatedAtDesc("PENDING_APPROVAL", pageable));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveAction(@PathVariable UUID id) {
        return healingActionRepository.findById(id).map(action -> {
            if (!"PENDING_APPROVAL".equals(action.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Action is not pending approval."));
            }
            
            // Update in DB
            action.setStatus("QUEUED");
            action.setStartedAt(Instant.now());
            healingActionRepository.save(action);
            
            // Publish to Kafka (In a real system we would emit a specific Approval event for the HealingService to consume)
            // Note: The PolicyEngine already listens to POLICY_DECISIONS. We can re-publish a decision with APPROVED status.
            Map<String, Object> approvalEvent = Map.of(
                    "healingActionId", action.getId(),
                    "incidentId", action.getIncidentId(),
                    "actionType", action.getActionType(),
                    "targetService", action.getTargetService(),
                    "approvalStatus", "APPROVED",
                    "approvedBy", "UI_ADMIN_OVERRIDE"
            );
            
            kafkaTemplate.send("sre.policy.decisions", action.getTargetService(), approvalEvent);
            
            return ResponseEntity.ok(Map.of("message", "Action approved and queued for execution."));
        }).orElse(ResponseEntity.notFound().build());
    }
}
