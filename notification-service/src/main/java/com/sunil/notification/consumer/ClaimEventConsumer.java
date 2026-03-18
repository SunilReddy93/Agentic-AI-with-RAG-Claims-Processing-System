package com.sunil.notification.consumer;

import com.sunil.notification.event.ClaimEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ClaimEventConsumer {

    @KafkaListener(topics = "claim-events", groupId = "notification-group")
    public void consumeClaimEvent(ClaimEvent event) {
        log.info("Received claim event: {} for claimId: {}",
                event.getEventType(), event.getClaimId());

        String notification = generateNotification(event);

        log.info("Sending notification to userId {}: {}",
                event.getUserId(), notification);
    }

    private String generateNotification(ClaimEvent event) {
        return switch (event.getStatus()) {
            case SUBMITTED -> String.format(
                    "Dear Customer, your %s claim (ID: %d) has been successfully submitted. " +
                            "Estimated amount: ₹%.2f. We will review it shortly.",
                    event.getClaimType(), event.getClaimId(), event.getEstimatedAmount());

            case UNDER_REVIEW -> String.format(
                    "Dear Customer, your claim (ID: %d) is now under review. " +
                            "Our team is carefully examining your case.",
                    event.getClaimId());

            case APPROVED -> String.format(
                    "Dear Customer, great news! Your claim (ID: %d) has been approved. " +
                            "Settlement will be processed shortly.",
                    event.getClaimId());

            case REJECTED -> String.format(
                    "Dear Customer, unfortunately your claim (ID: %d) has been rejected. " +
                            "Please contact our support team for more information.",
                    event.getClaimId());

            case SETTLED -> String.format(
                    "Dear Customer, your claim (ID: %d) has been settled. " +
                            "Amount of ₹%.2f has been processed. Thank you for choosing us.",
                    event.getClaimId(), event.getEstimatedAmount());
        };
    }
}