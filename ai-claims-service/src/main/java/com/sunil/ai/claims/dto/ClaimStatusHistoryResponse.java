package com.sunil.ai.claims.dto;

import com.sunil.ai.claims.enums.ClaimStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimStatusHistoryResponse {

    private Long id;
    private ClaimStatus fromStatus;
    private ClaimStatus toStatus;
    private String changedBy;
    private String remarks;
    private LocalDateTime changedAt;
}