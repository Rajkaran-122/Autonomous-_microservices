package com.ai.sre.gateway.controller;

import com.ai.sre.gateway.model.IncidentEntity;
import com.ai.sre.gateway.repository.IncidentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/incidents")
public class IncidentController {

    private final IncidentRepository incidentRepository;

    public IncidentController(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    @GetMapping
    public ResponseEntity<Page<IncidentEntity>> listIncidents(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(incidentRepository.findAllByOrderByDetectedAtDesc(pageable));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<IncidentEntity>> getRecentIncidents() {
        return ResponseEntity.ok(incidentRepository.findTop5ByOrderByDetectedAtDesc());
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncidentEntity> getIncident(@PathVariable UUID id) {
        return incidentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
