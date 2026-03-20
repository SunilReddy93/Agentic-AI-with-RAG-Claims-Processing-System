package com.sunil.claims.decision.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClaimDecisionEventPublisher {

    private final KafkaTemplate<String, ClaimDecisionEvent> kafkaTemplate;

    @Value("${kafka.topic.claim-decisions:claim-decisions}")
    private String claimDecisionsTopic;

    public void publishDecision(ClaimDecisionEvent event) {
        log.info("Publishing decision event for claim: {} — decision: {}",
                event.getClaimId(), event.getDecision());

        kafkaTemplate.send(claimDecisionsTopic,
                String.valueOf(event.getClaimId()), event);

        log.info("Decision event published successfully for claim: {}", event.getClaimId());
    }
}