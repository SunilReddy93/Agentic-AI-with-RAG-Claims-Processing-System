package com.sunil.claims.decision.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimDecisionRequest {

    private Long claimId;
    private Long userId;
    private String claimType;
    private String description;
    private String incidentDetails;
    private Double estimatedAmount;
    private String fraudRisk;
    private String aiSummary;
}