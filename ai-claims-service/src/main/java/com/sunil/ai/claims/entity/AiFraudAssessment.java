package com.sunil.ai.claims.entity;

import com.sunil.ai.claims.enums.FraudRisk;
import com.sunil.ai.claims.enums.Priority;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_fraud_assessment")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiFraudAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "claim_id", nullable = false)
    private Claim claim;

    @Enumerated(EnumType.STRING)
    @Column(name = "fraud_risk", nullable = false)
    private FraudRisk fraudRisk;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    @Column(name = "ai_summary", nullable = false, columnDefinition = "TEXT")
    private String aiSummary;

    @CreationTimestamp
    @Column(name = "assessed_at", updatable = false)
    private LocalDateTime assessedAt;
}