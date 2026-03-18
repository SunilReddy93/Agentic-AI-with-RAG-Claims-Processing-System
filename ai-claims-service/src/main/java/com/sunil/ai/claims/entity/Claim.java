package com.sunil.ai.claims.entity;

import com.sunil.ai.claims.enums.ClaimStatus;
import com.sunil.ai.claims.enums.ClaimType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Table(name = "claims", indexes = {
        @Index(name = "idx_claim_user_id", columnList = "user_id"),
        @Index(name = "idx_claim_status", columnList = "status"),
        @Index(name = "idx_claim_idempotency_key", columnList = "idempotency_key")
})
@Getter
@Setter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "claim_type", nullable = false)
    private ClaimType claimType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClaimStatus status;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @OneToOne(mappedBy = "claim", cascade = CascadeType.ALL)
    private ClaimDetail claimDetail;

    @OneToOne(mappedBy = "claim", cascade = CascadeType.ALL)
    private AiFraudAssessment aiFraudAssessment;

    @CreationTimestamp
    @Column(name = "submitted_at", updatable = false)
    private LocalDateTime submittedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
