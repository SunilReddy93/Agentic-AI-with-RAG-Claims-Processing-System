package com.sunil.notification.consumer;

import com.sunil.notification.event.ClaimEvent;
import com.sunil.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClaimEventConsumer {

    private final EmailService emailService;

    @KafkaListener(topics = "claim-events", groupId = "notification-group")
    public void consumeClaimEvent(ClaimEvent event) {
        log.info("Received claim event: {} for claimId: {}",
                event.getEventType(), event.getClaimId());

        String notification = generateNotification(event);
        String subject = generateSubject(event);

        log.info("Sending notification to userId {}: {}", event.getUserId(), notification);

        emailService.sendEmail(subject, notification);
    }

    private String generateSubject(ClaimEvent event) {
        return switch (event.getStatus()) {
            case SUBMITTED -> String.format("Claim #%d Submitted Successfully", event.getClaimId());
            case UNDER_REVIEW -> String.format("Claim #%d is Under Review", event.getClaimId());
            case APPROVED -> String.format("Claim #%d Approved!", event.getClaimId());
            case REJECTED -> String.format("Claim #%d Rejected", event.getClaimId());
            case SETTLED -> String.format("Claim #%d Settled", event.getClaimId());
        };
    }

    private String generateNotification(ClaimEvent event) {
        return switch (event.getStatus()) {
            case SUBMITTED -> String.format("""
                    Dear Customer,
                    
                    Your %s claim (ID: %d) has been successfully submitted.
                    Estimated amount: ₹%.2f.
                    
                    We will review it shortly and keep you updated.
                    
                    Thank you,
                    Claims Team
                    """,
                    event.getClaimType(), event.getClaimId(), event.getEstimatedAmount());

            case UNDER_REVIEW -> String.format("""
                    Dear Customer,
                    
                    Your claim (ID: %d) is now under review.
                    Our team is carefully examining your case.
                    
                    We will notify you once a decision is made.
                    
                    Thank you,
                    Claims Team
                    """,
                    event.getClaimId());

            case APPROVED -> String.format("""
                    Dear Customer,
                    
                    Great news! Your claim (ID: %d) has been approved.
                    Settlement will be processed shortly.
                    
                    Thank you for choosing us.
                    Claims Team
                    """,
                    event.getClaimId());

            case REJECTED -> String.format("""
                    Dear Customer,
                    
                    Unfortunately your claim (ID: %d) has been rejected.
                    Please contact our support team for more information.
                    
                    Thank you,
                    Claims Team
                    """,
                    event.getClaimId());

            case SETTLED -> String.format("""
                    Dear Customer,
                    
                    Your claim (ID: %d) has been settled.
                    Amount of ₹%.2f has been processed.
                    
                    Thank you for choosing us.
                    Claims Team
                    """,
                    event.getClaimId(), event.getEstimatedAmount());
        };
    }
}