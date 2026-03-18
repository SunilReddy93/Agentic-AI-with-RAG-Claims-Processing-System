package com.sunil.ai.claims.event;

import com.sunil.ai.claims.enums.ClaimStatus;
import com.sunil.ai.claims.enums.ClaimType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimEvent {

    private String eventId;
    private String eventType;
    private Long claimId;
    private Long userId;
    private ClaimType claimType;
    private ClaimStatus status;
    private String incidentDescription;
    private BigDecimal estimatedAmount;
    private LocalDateTime occurredAt;
}