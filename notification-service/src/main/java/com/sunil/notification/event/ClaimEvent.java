package com.sunil.notification.event;

import com.sunil.notification.enums.ClaimStatus;
import com.sunil.notification.enums.ClaimType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
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