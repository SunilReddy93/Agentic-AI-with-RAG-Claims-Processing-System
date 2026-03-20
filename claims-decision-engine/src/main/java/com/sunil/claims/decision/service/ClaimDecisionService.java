package com.sunil.claims.decision.service;

import com.sunil.claims.decision.dto.ClaimDecisionRequest;
import com.sunil.claims.decision.dto.ClaimDecisionResponse;
import com.sunil.claims.decision.tools.ActionTool;
import com.sunil.claims.decision.tools.RagSearchTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaimDecisionService {

    private final ChatClient.Builder chatClientBuilder;
    private final RagSearchTool ragSearchTool;
    private final ActionTool actionTool;

    public ClaimDecisionResponse processClaimDecision(ClaimDecisionRequest request) {
        log.info("Processing claim decision for claimId: {}", request.getClaimId());

        ChatClient chatClient = chatClientBuilder
                .defaultTools(ragSearchTool, actionTool)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(
                                MessageWindowChatMemory.builder()
                                        .chatMemoryRepository(new InMemoryChatMemoryRepository())
                                        .maxMessages(10)
                                        .build())
                        .build())
                .build();

        String agentPrompt = buildAgentPrompt(request);

        log.info("Sending claim to agent for decision...");

        String agentResponse = chatClient.prompt()
                .user(agentPrompt)
                .call()
                .content();

        log.info("Agent response received for claimId: {}", request.getClaimId());

        return ClaimDecisionResponse.builder()
                .claimId(request.getClaimId())
                .decision(agentResponse)
                .reasoning("Agent processed claim using RAG + Groq AI")
                .nextAction("Check Kafka topic for decision event")
                .build();
    }

    private String buildAgentPrompt(ClaimDecisionRequest request) {
        return String.format("""
                You are an intelligent insurance claims processing agent.
                
                Analyze the following insurance claim and make a decision:
                
                Claim ID: %d
                Claim Type: %s
                Description: %s
                Incident Details: %s
                Estimated Amount: $%.2f
                Initial Fraud Risk: %s
                AI Summary: %s
                
                Follow these steps in order:
                1. Search for similar past claims using searchSimilarClaims tool
                2. Search for relevant policy rules using searchPolicyRules tool
                3. Search for compliance rules using searchComplianceRules tool
                4. Based on all findings, make ONE of these decisions:
                   - If fraud risk is LOW and claim is valid → call autoApproveClaim
                   - If fraud risk is HIGH or amount > $100,000 → call escalateToUnderwriter
                   - If details are unclear or missing → call requestMoreInfo
                   - For HIGH risk claims → also call generateFraudReport
                
                Always explain your reasoning clearly.
                """,
                request.getClaimId(),
                request.getClaimType(),
                request.getDescription(),
                request.getIncidentDetails(),
                request.getEstimatedAmount(),
                request.getFraudRisk(),
                request.getAiSummary()
        );
    }
}