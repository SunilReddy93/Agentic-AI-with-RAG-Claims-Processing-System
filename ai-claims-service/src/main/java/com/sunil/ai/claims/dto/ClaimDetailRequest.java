package com.sunil.ai.claims.dto;

import com.sunil.ai.claims.enums.CauseType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimDetailRequest {

    @NotBlank(message = "Damage code is required")
    private String damageCode;

    @NotBlank(message = "Damaged item is required")
    private String damagedItem;

    @NotNull(message = "Incident date is required")
    private LocalDate incidentDate;

    @NotBlank(message = "Incident location is required")
    private String incidentLocation;

    @NotBlank(message = "Incident description is required")
    private String incidentDescription;

    @NotNull(message = "Cause type is required")
    private CauseType causeType;

    @NotNull(message = "Estimated amount is required")
    @Positive(message = "Estimated amount must be positive")
    private BigDecimal estimatedAmount;
}