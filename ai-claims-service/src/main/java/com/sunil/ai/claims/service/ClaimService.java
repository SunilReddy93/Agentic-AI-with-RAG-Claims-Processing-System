package com.sunil.ai.claims.service;

import com.sunil.ai.claims.client.ClaimsDecisionEngineClient;
import com.sunil.ai.claims.client.GroqAiClient;
import com.sunil.ai.claims.client.UserManagementClient;
import com.sunil.ai.claims.dto.*;
import com.sunil.ai.claims.entity.*;
import com.sunil.ai.claims.enums.ClaimStatus;
import com.sunil.ai.claims.enums.FraudRisk;
import com.sunil.ai.claims.event.ClaimEvent;
import com.sunil.ai.claims.event.ClaimEventProducer;
import com.sunil.ai.claims.exception.BusinessException;
import com.sunil.ai.claims.model.AiFraudAssessmentResult;
import com.sunil.ai.claims.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final ClaimDetailRepository claimDetailRepository;
    private final AiFraudAssessmentRepository aiFraudAssessmentRepository;
    private final ClaimStatusHistoryRepository claimStatusHistoryRepository;
    private final UserManagementClient userManagementClient;
    private final GroqAiClient groqAiClient;
    private final ClaimEventProducer claimEventProducer;
    private final ClaimsDecisionEngineClient claimsDecisionEngineClient;

    @Value("${internal.api.key}")
    private String internalApiKey;

    @Transactional
    public ClaimResponse submitClaim(ClaimRequest request, Long userId) {

        // Step 1 - Check idempotency
        claimRepository.findByIdempotencyKey(request.getIdempotencyKey())
                .ifPresent(existing -> {
                    throw new BusinessException(
                            "Claim already exists with this idempotency key",
                            HttpStatus.CONFLICT);
                });

        // Step 2 - Verify user exists and is active
        UserResponse user = userManagementClient.getUserById(userId, internalApiKey);
        if (user == null || !user.getStatus().equals("ACTIVE")) {
            throw new BusinessException("User account not found or is inactive", HttpStatus.FORBIDDEN);
        }

        // Step 3 - Call Groq AI for fraud assessment
        AiFraudAssessmentResult assessmentResult = groqAiClient.assessFraud(
                request.getClaimDetail(),
                request.getClaimType().name()
        );

        // Step 4 - Save claim
        Claim claim = Claim.builder()
                .userId(userId)
                .claimType(request.getClaimType())
                .status(ClaimStatus.SUBMITTED)
                .idempotencyKey(request.getIdempotencyKey())
                .build();

        Claim savedClaim = claimRepository.save(claim);

        // Step 5 - Save claim details
        ClaimDetail claimDetail = ClaimDetail.builder()
                .claim(savedClaim)
                .damageCode(request.getClaimDetail().getDamageCode())
                .damagedItem(request.getClaimDetail().getDamagedItem())
                .incidentDate(request.getClaimDetail().getIncidentDate())
                .incidentLocation(request.getClaimDetail().getIncidentLocation())
                .incidentDescription(request.getClaimDetail().getIncidentDescription())
                .causeType(request.getClaimDetail().getCauseType())
                .estimatedAmount(request.getClaimDetail().getEstimatedAmount())
                .build();

        claimDetailRepository.save(claimDetail);

        // Step 6 - Save AI fraud assessment
        AiFraudAssessment aiFraudAssessment = AiFraudAssessment.builder()
                .claim(savedClaim)
                .fraudRisk(assessmentResult.getFraudRisk())
                .priority(assessmentResult.getPriority())
                .aiSummary(assessmentResult.getSummary())
                .build();

        aiFraudAssessmentRepository.save(aiFraudAssessment);

        // Step 7 - Trigger AI agent decision engine asynchronously
        claimsDecisionEngineClient.triggerDecision(
                savedClaim.getId(),
                userId,
                request.getClaimType().name(),
                request.getClaimDetail().getIncidentDescription(),
                request.getClaimDetail().getIncidentLocation(),
                request.getClaimDetail().getEstimatedAmount().doubleValue(),
                assessmentResult.getFraudRisk().name(),
                assessmentResult.getSummary()
        );

        // Step 7 - Save status history
        saveStatusHistory(savedClaim, null, ClaimStatus.SUBMITTED,
                "system", "Claim submitted");

        // Step 8 - Publish Kafka event
        claimEventProducer.publishClaimEvent(ClaimEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("CLAIM_SUBMITTED")
                .claimId(savedClaim.getId())
                .userId(userId)
                .claimType(savedClaim.getClaimType())
                .status(ClaimStatus.SUBMITTED)
                .incidentDescription(request.getClaimDetail().getIncidentDescription())
                .estimatedAmount(request.getClaimDetail().getEstimatedAmount())
                .occurredAt(LocalDateTime.now())
                .build());

        log.info("Claim submitted successfully for userId: {}", userId);

        return mapToClaimResponse(savedClaim, claimDetail, aiFraudAssessment);
    }

    @Cacheable(value = "claims", key = "#claimId")
    public ClaimResponse getClaimById(Long claimId, Long userId, String role) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new BusinessException(
                        "Claim not found", HttpStatus.NOT_FOUND));

        if (!role.equals("ROLE_ADMIN") && !claim.getUserId().equals(userId)) {
            throw new BusinessException(
                    "You are not authorized to view this claim", HttpStatus.FORBIDDEN);
        }

        return mapToClaimResponse(claim, claim.getClaimDetail(), claim.getAiFraudAssessment());
    }

    public List<ClaimResponse> getMyClaims(Long userId) {
        return claimRepository.findByUserId(userId)
                .stream()
                .map(claim -> mapToClaimResponse(
                        claim, claim.getClaimDetail(), claim.getAiFraudAssessment()))
                .collect(Collectors.toList());
    }

    @Transactional
    public ClaimResponse updateClaimStatus(Long claimId, ClaimStatus newStatus,
                                           String adminUsername, String remarks) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new BusinessException(
                        "Claim not found", HttpStatus.NOT_FOUND));

        // Block approval if AI fraud risk is HIGH
        if (newStatus == ClaimStatus.APPROVED) {
            AiFraudAssessment assessment = claim.getAiFraudAssessment();
            if (assessment != null && assessment.getFraudRisk() == FraudRisk.HIGH) {
                throw new BusinessException(
                        "Claim cannot be approved. High fraud risk detected. Underwriter authorization required.",
                        HttpStatus.UNPROCESSABLE_ENTITY);
            }
        }

        ClaimStatus previousStatus = claim.getStatus();
        claim.setStatus(newStatus);
        Claim updatedClaim = claimRepository.save(claim);

        saveStatusHistory(updatedClaim, previousStatus, newStatus, adminUsername, remarks);

        claimEventProducer.publishClaimEvent(ClaimEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("CLAIM_" + newStatus.name())
                .claimId(updatedClaim.getId())
                .userId(updatedClaim.getUserId())
                .claimType(updatedClaim.getClaimType())
                .status(newStatus)
                .incidentDescription(updatedClaim.getClaimDetail().getIncidentDescription())
                .estimatedAmount(updatedClaim.getClaimDetail().getEstimatedAmount())
                .occurredAt(LocalDateTime.now())
                .build());

        log.info("Claim {} status updated to {} by {}", claimId, newStatus, adminUsername);

        return mapToClaimResponse(updatedClaim,
                updatedClaim.getClaimDetail(), updatedClaim.getAiFraudAssessment());
    }

    public List<ClaimStatusHistoryResponse> getClaimHistory(Long claimId,
                                                            Long userId, String role) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new BusinessException(
                        "Claim not found", HttpStatus.NOT_FOUND));

        if (!role.equals("ROLE_ADMIN") && !claim.getUserId().equals(userId)) {
            throw new BusinessException(
                    "You are not authorized to view this claim history",
                    HttpStatus.FORBIDDEN);
        }

        return claimStatusHistoryRepository
                .findByClaimIdOrderByChangedAtAsc(claimId)
                .stream()
                .map(this::mapToStatusHistoryResponse)
                .collect(Collectors.toList());
    }

    private void saveStatusHistory(Claim claim, ClaimStatus fromStatus,
                                   ClaimStatus toStatus, String changedBy, String remarks) {
        ClaimStatusHistory history = ClaimStatusHistory.builder()
                .claim(claim)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .changedBy(changedBy)
                .remarks(remarks)
                .build();
        claimStatusHistoryRepository.save(history);
    }

    private ClaimResponse mapToClaimResponse(Claim claim, ClaimDetail claimDetail,
                                             AiFraudAssessment aiFraudAssessment) {
        return ClaimResponse.builder()
                .id(claim.getId())
                .userId(claim.getUserId())
                .claimType(claim.getClaimType())
                .status(claim.getStatus())
                .idempotencyKey(claim.getIdempotencyKey())
                .claimDetail(mapToClaimDetailResponse(claimDetail))
                .aiFraudAssessment(mapToAiFraudAssessmentResponse(aiFraudAssessment))
                .submittedAt(claim.getSubmittedAt())
                .updatedAt(claim.getUpdatedAt())
                .build();
    }

    private ClaimDetailResponse mapToClaimDetailResponse(ClaimDetail claimDetail) {
        if (claimDetail == null) return null;
        return ClaimDetailResponse.builder()
                .id(claimDetail.getId())
                .damageCode(claimDetail.getDamageCode())
                .damagedItem(claimDetail.getDamagedItem())
                .incidentDate(claimDetail.getIncidentDate())
                .incidentLocation(claimDetail.getIncidentLocation())
                .incidentDescription(claimDetail.getIncidentDescription())
                .causeType(claimDetail.getCauseType())
                .estimatedAmount(claimDetail.getEstimatedAmount())
                .build();
    }

    private AiFraudAssessmentResponse mapToAiFraudAssessmentResponse(
            AiFraudAssessment aiFraudAssessment) {
        if (aiFraudAssessment == null) return null;
        return AiFraudAssessmentResponse.builder()
                .id(aiFraudAssessment.getId())
                .fraudRisk(aiFraudAssessment.getFraudRisk())
                .priority(aiFraudAssessment.getPriority())
                .aiSummary(aiFraudAssessment.getAiSummary())
                .assessedAt(aiFraudAssessment.getAssessedAt())
                .build();
    }

    private ClaimStatusHistoryResponse mapToStatusHistoryResponse(ClaimStatusHistory history) {
        return ClaimStatusHistoryResponse.builder()
                .id(history.getId())
                .fromStatus(history.getFromStatus())
                .toStatus(history.getToStatus())
                .changedBy(history.getChangedBy())
                .remarks(history.getRemarks())
                .changedAt(history.getChangedAt())
                .build();
    }
}