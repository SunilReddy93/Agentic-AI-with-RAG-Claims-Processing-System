package com.sunil.ai.claims.dto;

import com.sunil.ai.claims.enums.CauseType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimDetailResponse {

    private Long id;
    private String damageCode;
    private String damagedItem;
    private LocalDate incidentDate;
    private String incidentLocation;
    private String incidentDescription;
    private CauseType causeType;
    private BigDecimal estimatedAmount;
}