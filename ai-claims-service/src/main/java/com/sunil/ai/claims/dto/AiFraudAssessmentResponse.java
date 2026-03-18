package com.sunil.ai.claims.dto;

import com.sunil.ai.claims.enums.FraudRisk;
import com.sunil.ai.claims.enums.Priority;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiFraudAssessmentResponse {

    private Long id;
    private FraudRisk fraudRisk;
    private Priority priority;
    private String aiSummary;
    private LocalDateTime assessedAt;
}