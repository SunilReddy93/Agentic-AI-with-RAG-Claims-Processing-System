package com.sunil.ai.claims.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunil.ai.claims.model.AiFraudAssessmentResult;
import com.sunil.ai.claims.dto.ClaimDetailRequest;
import com.sunil.ai.claims.dto.GroqAiResponse;
import com.sunil.ai.claims.enums.FraudRisk;
import com.sunil.ai.claims.enums.Priority;
import com.sunil.ai.claims.exception.ServiceNotAvailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroqAiClient {

    private final WebClient groqWebClient;
    private final ObjectMapper objectMapper;

    @Value("${groq.api.key}")
    private String groqApiKey;

    @Value("${groq.model}")
    private String groqModel;

    @CircuitBreaker(name = "groq-ai-cb", fallbackMethod = "fallback")
    public AiFraudAssessmentResult assessFraud(ClaimDetailRequest claimDetail, String claimType) {

        String prompt = buildPrompt(claimDetail, claimType);

        Map<String, Object> requestBody = Map.of(
                "model", groqModel,
                "messages", List.of(
                        Map.of("role", "system", "content",
                                "You are an insurance fraud detection expert. Always respond in valid JSON only. No explanation, no markdown."),
                        Map.of("role", "user", "content", prompt)
                )
        );

        GroqAiResponse response = groqWebClient.post()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + groqApiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(GroqAiResponse.class)
                .block();

        return parseResponse(response);
    }

    private String buildPrompt(ClaimDetailRequest claimDetail, String claimType) {
        return String.format("""
                Analyse this insurance claim for fraud risk:
                
                Claim Type: %s
                Damaged Item: %s
                Damage Code: %s
                Incident Date: %s
                Incident Location: %s
                Cause Type: %s
                Estimated Amount: %s
                Description: %s
                
                Respond ONLY with this JSON:
                {
                    "fraudRisk": "LOW" or "MEDIUM" or "HIGH",
                    "priority": "LOW" or "MEDIUM" or "HIGH",
                    "summary": "brief explanation"
                }
                """,
                claimType,
                claimDetail.getDamagedItem(),
                claimDetail.getDamageCode(),
                claimDetail.getIncidentDate(),
                claimDetail.getIncidentLocation(),
                claimDetail.getCauseType(),
                claimDetail.getEstimatedAmount(),
                claimDetail.getIncidentDescription()
        );
    }

    private AiFraudAssessmentResult parseResponse(GroqAiResponse response) {
        try {
            String content = response.getContent();
            return objectMapper.readValue(content, AiFraudAssessmentResult.class);
        } catch (Exception e) {
            log.error("Failed to parse Groq AI response: {}", e.getMessage());
            return getDefaultAssessment();
        }
    }

    public AiFraudAssessmentResult fallback(ClaimDetailRequest claimDetail,
                                            String claimType,
                                            Throwable throwable) {
        log.error("Groq AI service unavailable: {}", throwable.getMessage());
        return getDefaultAssessment();
    }

    private AiFraudAssessmentResult getDefaultAssessment() {
        return new AiFraudAssessmentResult(
                FraudRisk.MEDIUM,
                Priority.MEDIUM,
                "AI assessment unavailable. Manual review required."
        );
    }
}