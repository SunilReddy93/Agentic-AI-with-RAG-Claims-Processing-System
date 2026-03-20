package com.sunil.claims.decision.tools;

import com.sunil.claims.decision.enums.DecisionType;
import com.sunil.claims.decision.event.ClaimDecisionEvent;
import com.sunil.claims.decision.event.ClaimDecisionEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActionTool {

    private final ClaimDecisionEventPublisher eventPublisher;

    @Tool(description = """
        Auto-approve a claim when fraud risk is LOW and all checks pass.
        Use this when the claim is genuine and within policy coverage.
        """)
    public String autoApproveClaim(Long claimId, String reasoning) {
        log.info("Action Tool — Auto-approving claim: {}", claimId);

        eventPublisher.publishDecision(ClaimDecisionEvent.builder()
                .claimId(claimId)
                .decision(DecisionType.AUTO_APPROVED)
                .reasoning(reasoning)
                .build());

        return "Claim " + claimId + " has been AUTO-APPROVED. Reason: " + reasoning;
    }

    @Tool(description = """
        Escalate a claim to underwriter when fraud risk is HIGH or
        claim amount is large. Use this when manual review is needed.
        """)
    public String escalateToUnderwriter(Long claimId, String reasoning) {
        log.info("Action Tool — Escalating claim: {} to underwriter", claimId);

        eventPublisher.publishDecision(ClaimDecisionEvent.builder()
                .claimId(claimId)
                .decision(DecisionType.ESCALATED_TO_UNDERWRITER)
                .reasoning(reasoning)
                .build());

        return "Claim " + claimId + " has been ESCALATED TO UNDERWRITER. Reason: " + reasoning;
    }

    @Tool(description = """
        Request more information from the user when claim details
        are insufficient or suspicious. Use this when details are unclear.
        """)
    public String requestMoreInfo(Long claimId, String missingInfo) {
        log.info("Action Tool — Requesting more info for claim: {}", claimId);

        eventPublisher.publishDecision(ClaimDecisionEvent.builder()
                .claimId(claimId)
                .decision(DecisionType.REQUEST_MORE_INFO)
                .reasoning(missingInfo)
                .build());

        return "More information requested for claim " + claimId + ". Missing: " + missingInfo;
    }

    @Tool(description = """
        Generate a detailed fraud report for a claim.
        Use this for HIGH risk claims to document fraud indicators found.
        """)
    public String generateFraudReport(Long claimId, String fraudIndicators, String similarCases) {
        log.info("Action Tool — Generating fraud report for claim: {}", claimId);

        String report = String.format("""
                    FRAUD REPORT - Claim ID: %d
                    Fraud Indicators: %s
                    Similar Fraud Cases: %s
                    Recommendation: Escalate for manual review
                    """,
                claimId, fraudIndicators, similarCases
        );

        log.info("Fraud report generated for claim: {}", claimId);
        return report;
    }
}