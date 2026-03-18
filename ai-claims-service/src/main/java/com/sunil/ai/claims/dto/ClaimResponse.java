package com.sunil.ai.claims.dto;

import com.sunil.ai.claims.enums.ClaimStatus;
import com.sunil.ai.claims.enums.ClaimType;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimResponse {

    private Long id;
    private Long userId;
    private ClaimType claimType;
    private ClaimStatus status;
    private String idempotencyKey;
    private ClaimDetailResponse claimDetail;
    private AiFraudAssessmentResponse aiFraudAssessment;
    private LocalDateTime submittedAt;
    private LocalDateTime updatedAt;
}