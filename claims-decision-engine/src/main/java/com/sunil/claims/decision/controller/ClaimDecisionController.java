package com.sunil.claims.decision.controller;

import com.sunil.claims.decision.dto.ClaimDecisionRequest;
import com.sunil.claims.decision.dto.ClaimDecisionResponse;
import com.sunil.claims.decision.service.ClaimDecisionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/decisions")
@RequiredArgsConstructor
public class ClaimDecisionController {

    private final ClaimDecisionService claimDecisionService;

    @PostMapping("/process")
    public ResponseEntity<ClaimDecisionResponse> processClaimDecision(
            @Valid @RequestBody ClaimDecisionRequest request) {
        log.info("Received claim decision request for claimId: {}", request.getClaimId());
        ClaimDecisionResponse response = claimDecisionService.processClaimDecision(request);
        return ResponseEntity.ok(response);
    }
}