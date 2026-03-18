package com.sunil.ai.claims.model;

import com.sunil.ai.claims.enums.FraudRisk;
import com.sunil.ai.claims.enums.Priority;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
@AllArgsConstructor
public class AiFraudAssessmentResult {

    private FraudRisk fraudRisk;
    private Priority priority;
    private String summary;

}
