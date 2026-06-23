package com.ai.sre.gateway.controller;

import com.ai.sre.gateway.model.SloViolationEntity;
import com.ai.sre.gateway.repository.SloViolationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/slo")
public class SloController {

    private final SloViolationRepository sloViolationRepository;

    public SloController(SloViolationRepository sloViolationRepository) {
        this.sloViolationRepository = sloViolationRepository;
    }

    @GetMapping
    public ResponseEntity<Page<SloViolationEntity>> listViolations(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(sloViolationRepository.findAllByOrderByStartedAtDesc(pageable));
    }
}
