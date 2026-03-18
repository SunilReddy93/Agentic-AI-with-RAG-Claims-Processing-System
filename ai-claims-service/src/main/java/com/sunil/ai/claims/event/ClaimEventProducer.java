package com.sunil.ai.claims.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClaimEventProducer {

    private static final String TOPIC = "claim-events";

    private final KafkaTemplate<String, ClaimEvent> kafkaTemplate;

    public void publishClaimEvent(ClaimEvent event) {
        try{
            kafkaTemplate.send(TOPIC, String.valueOf(event.getClaimId()), event);
            log.info("Published claim event: {} for claimId: {}",
                    event.getEventType(), event.getClaimId());
        } catch (Exception e) {
            log.error("Failed to publish Kafka event for claimId: {}. Error: {}",
                    event.getClaimId(), e.getMessage());
        }

    }
}