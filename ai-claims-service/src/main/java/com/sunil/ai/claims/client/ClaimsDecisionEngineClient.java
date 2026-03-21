package com.sunil.ai.claims.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClaimsDecisionEngineClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${claims.decision.engine.url}")
    private String claimsDecisionEngineUrl;

    public void triggerDecision(Long claimId, Long userId, String claimType,
                                String description, String incidentDetails,
                                Double estimatedAmount, String fraudRisk,
                                String aiSummary) {
        try {
            log.info("Triggering claims decision engine for claimId: {}", claimId);

            Map<String, Object> request = Map.of(
                    "claimId", claimId,
                    "userId", userId,
                    "claimType", claimType,
                    "description", description,
                    "incidentDetails", incidentDetails,
                    "estimatedAmount", estimatedAmount,
                    "fraudRisk", fraudRisk,
                    "aiSummary", aiSummary
            );

            webClientBuilder.build()
                    .post()
                    .uri(claimsDecisionEngineUrl + "/api/v1/decisions/process")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .subscribe(
                            response -> log.info("Decision engine response for claimId {}: {}",
                                    claimId, response),
                            error -> log.error("Decision engine call failed for claimId {}: {}",
                                    claimId, error.getMessage())
                    );

            log.info("Decision engine triggered asynchronously for claimId: {}", claimId);

        } catch (Exception e) {
            log.error("Failed to trigger decision engine for claimId {}: {}",
                    claimId, e.getMessage());
        }
    }
}