package com.sunil.claims.decision.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimDecisionResponse {

    private Long claimId;
    private String decision;
    private String reasoning;
    private String fraudReport;
    private String similarCasesFound;
    private String nextAction;
    private Double confidenceScore;
}