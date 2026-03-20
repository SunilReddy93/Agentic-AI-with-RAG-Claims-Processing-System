package com.sunil.claims.decision.event;

import com.sunil.claims.decision.enums.DecisionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimDecisionEvent {

    private Long claimId;
    private DecisionType decision;
    private String reasoning;
}