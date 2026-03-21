package com.sunil.notification.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaimDecisionEvent {

    private Long claimId;
    private String decision;
    private String reasoning;
    private String decisionSummary;
}