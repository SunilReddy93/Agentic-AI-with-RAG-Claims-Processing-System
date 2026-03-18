package com.sunil.ai.claims.dto;

import com.sunil.ai.claims.enums.ClaimType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimRequest {

    @NotNull(message = "Claim type is required")
    private ClaimType claimType;

    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;

    @NotNull(message = "Claim details are required")
    @Valid
    private ClaimDetailRequest claimDetail;
}