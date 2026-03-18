package com.sunil.ai.claims.controller;

import com.sunil.ai.claims.dto.*;
import com.sunil.ai.claims.enums.ClaimStatus;
import com.sunil.ai.claims.exception.BusinessException;
import com.sunil.ai.claims.security.util.ClaimsPrincipal;
import com.sunil.ai.claims.service.ClaimService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService claimService;

    @PostMapping("/submit")
    public ResponseEntity<ClaimResponse> submitClaim(
            @Valid @RequestBody ClaimRequest request,
            @AuthenticationPrincipal ClaimsPrincipal principal) {
        ClaimResponse response = claimService.submitClaim(request, principal.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{claimId}")
    public ResponseEntity<ClaimResponse> getClaimById(
            @PathVariable Long claimId,
            @AuthenticationPrincipal ClaimsPrincipal principal) {
        ClaimResponse response = claimService.getClaimById(
                claimId, principal.getUserId(), principal.getRole());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-claims")
    public ResponseEntity<List<ClaimResponse>> getMyClaims(
            @AuthenticationPrincipal ClaimsPrincipal principal) {
        List<ClaimResponse> response = claimService.getMyClaims(principal.getUserId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{claimId}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClaimResponse> markUnderReview(
            @PathVariable Long claimId,
            @AuthenticationPrincipal ClaimsPrincipal principal) {
        ClaimResponse response = claimService.updateClaimStatus(
                claimId, ClaimStatus.UNDER_REVIEW,
                principal.getUsername(), "Claim is under review");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{claimId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClaimResponse> approveClaim(
            @PathVariable Long claimId,
            @AuthenticationPrincipal ClaimsPrincipal principal) {
        ClaimResponse response = claimService.updateClaimStatus(
                claimId, ClaimStatus.APPROVED,
                principal.getUsername(), "Claim approved");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{claimId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClaimResponse> rejectClaim(
            @PathVariable Long claimId,
            @RequestParam String reason,
            @AuthenticationPrincipal ClaimsPrincipal principal) {
        ClaimResponse response = claimService.updateClaimStatus(
                claimId, ClaimStatus.REJECTED,
                principal.getUsername(), reason);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{claimId}/settle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClaimResponse> settleClaim(
            @PathVariable Long claimId,
            @AuthenticationPrincipal ClaimsPrincipal principal) {
        ClaimResponse response = claimService.updateClaimStatus(
                claimId, ClaimStatus.SETTLED,
                principal.getUsername(), "Claim settled");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{claimId}/history")
    public ResponseEntity<List<ClaimStatusHistoryResponse>> getClaimHistory(
            @PathVariable Long claimId,
            @AuthenticationPrincipal ClaimsPrincipal principal) {
        List<ClaimStatusHistoryResponse> response = claimService.getClaimHistory(
                claimId, principal.getUserId(), principal.getRole());
        return ResponseEntity.ok(response);
    }
}