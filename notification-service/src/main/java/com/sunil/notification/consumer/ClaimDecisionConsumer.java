package com.sunil.notification.consumer;

import com.sunil.notification.event.ClaimDecisionEvent;
import com.sunil.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClaimDecisionConsumer {

    private final EmailService emailService;

    @KafkaListener(
            topics = "claim-decisions",
            groupId = "decision-notification-group",
            properties = {
                    "spring.json.value.default.type=com.sunil.notification.event.ClaimDecisionEvent"
            }
    )
    public void consumeClaimDecisionEvent(ClaimDecisionEvent event) {
        log.info("Received claim decision event for claimId: {} — decision: {}",
                event.getClaimId(), event.getDecision());

        String subject = generateSubject(event);
        String body = generateBody(event);

        emailService.sendEmail(subject, body);
    }

    private String generateSubject(ClaimDecisionEvent event) {
        return String.format("Claim #%d — Decision: %s", event.getClaimId(), event.getDecision());
    }

    private String generateBody(ClaimDecisionEvent event) {
        return String.format("""
            Dear Customer,
            
            Our AI agent has made a decision on your claim.
            
            Claim ID: %d
            Decision: %s
            
            Reasoning:
            %s
            
            If you have any questions please contact our support team.
            
            Thank you,
            Claims Team
            """,
                event.getClaimId(),
                event.getDecisionSummary() != null ? event.getDecisionSummary() : event.getDecision(),
                event.getReasoning()
        );
    }
}