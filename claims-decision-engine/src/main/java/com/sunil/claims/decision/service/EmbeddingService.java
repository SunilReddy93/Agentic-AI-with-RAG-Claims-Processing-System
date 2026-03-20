package com.sunil.claims.decision.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService implements ApplicationRunner {

    private final VectorStore vectorStore;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Checking if vector store needs seeding...");

        List<Document> existing = vectorStore.similaritySearch("insurance claim");

        if (existing != null && !existing.isEmpty()) {
            log.info("Vector store already seeded — skipping.");
            return;
        }

        log.info("Seeding vector store with sample data...");
        seedPastClaims();
        seedPolicyDocuments();
        seedComplianceRules();
        log.info("Vector store seeding completed!");
    }

    private void seedPastClaims() {
        List<Document> pastClaims = List.of(
                new Document("""
                        Vehicle accident claim — claimant reported rear-end collision on highway.
                        Damage estimated at $8,000. Police report filed. Claim approved. LOW fraud risk.
                        """,
                        Map.of("type", "past_claim", "category", "VEHICLE", "outcome", "APPROVED")
                ),
                new Document("""
                        Medical claim — claimant reported emergency surgery after accident.
                        Hospital bills totaling $45,000. All documents verified. Claim approved. LOW fraud risk.
                        """,
                        Map.of("type", "past_claim", "category", "MEDICAL", "outcome", "APPROVED")
                ),
                new Document("""
                        Property damage claim — claimant reported fire damage to home.
                        Estimated loss $120,000. Fire department report inconsistent with damage.
                        HIGH fraud risk. Escalated to underwriter.
                        """,
                        Map.of("type", "past_claim", "category", "PROPERTY", "outcome", "ESCALATED")
                ),
                new Document("""
                        Vehicle theft claim — claimant reported car stolen.
                        Same claimant filed similar theft claim 6 months ago.
                        HIGH fraud risk. Investigation initiated.
                        """,
                        Map.of("type", "past_claim", "category", "VEHICLE", "outcome", "ESCALATED")
                ),
                new Document("""
                        Medical claim — claimant reported multiple injuries from slip and fall.
                        Medical records inconsistent with reported injuries.
                        MEDIUM fraud risk. Additional documents requested.
                        """,
                        Map.of("type", "past_claim", "category", "MEDICAL", "outcome", "MORE_INFO")
                ),
                new Document("""
                        Life insurance claim — beneficiary filed claim after policyholder death.
                        Death certificate verified. Policy active for 5 years. Claim approved. LOW fraud risk.
                        """,
                        Map.of("type", "past_claim", "category", "LIFE", "outcome", "APPROVED")
                ),
                new Document("""
                        Property claim — water damage from burst pipe.
                        Plumber report and photos submitted. Damage estimated $15,000.
                        Claim approved. LOW fraud risk.
                        """,
                        Map.of("type", "past_claim", "category", "PROPERTY", "outcome", "APPROVED")
                ),
                new Document("""
                        Vehicle claim — claimant reported collision but no police report filed.
                        Damage inconsistent with reported accident. MEDIUM fraud risk.
                        Additional evidence requested.
                        """,
                        Map.of("type", "past_claim", "category", "VEHICLE", "outcome", "MORE_INFO")
                )
        );

        vectorStore.add(pastClaims);
        log.info("Seeded {} past claims", pastClaims.size());
    }

    private void seedPolicyDocuments() {
        List<Document> policyDocs = List.of(
                new Document("""
                        Vehicle insurance policy — covers collision damage, theft, and third party liability.
                        Maximum coverage $50,000 per incident. Police report required for theft claims.
                        Deductible $500. Claims must be filed within 30 days of incident.
                        """,
                        Map.of("type", "policy", "category", "VEHICLE")
                ),
                new Document("""
                        Medical insurance policy — covers hospitalization, surgery, and emergency treatment.
                        Maximum coverage $100,000 per year. Pre-existing conditions excluded.
                        Requires doctor referral for specialist treatment.
                        Claims must include original hospital bills and medical reports.
                        """,
                        Map.of("type", "policy", "category", "MEDICAL")
                ),
                new Document("""
                        Property insurance policy — covers fire, flood, theft and accidental damage.
                        Maximum coverage $500,000. Structural damage requires independent assessment.
                        Contents coverage up to $50,000. Claims must be filed within 60 days.
                        """,
                        Map.of("type", "policy", "category", "PROPERTY")
                ),
                new Document("""
                        Life insurance policy — pays death benefit to named beneficiary.
                        Coverage amount as per policy schedule.
                        Suicide exclusion applies for first 2 years.
                        Death certificate and policy documents required for claim.
                        """,
                        Map.of("type", "policy", "category", "LIFE")
                )
        );

        vectorStore.add(policyDocs);
        log.info("Seeded {} policy documents", policyDocs.size());
    }

    private void seedComplianceRules() {
        List<Document> complianceRules = List.of(
                new Document("""
                        Fraud indicator — multiple claims filed within short period by same claimant.
                        Flag for investigation if more than 2 claims in 6 months.
                        """,
                        Map.of("type", "compliance", "category", "FRAUD_INDICATOR")
                ),
                new Document("""
                        Fraud indicator — claim amount significantly higher than market value of damaged item.
                        Flag if claimed amount exceeds market value by more than 20%.
                        """,
                        Map.of("type", "compliance", "category", "FRAUD_INDICATOR")
                ),
                new Document("""
                        Compliance rule — all claims above $100,000 must be reviewed by senior underwriter.
                        Cannot be auto-approved regardless of fraud risk score.
                        """,
                        Map.of("type", "compliance", "category", "APPROVAL_RULE")
                ),
                new Document("""
                        Compliance rule — claims with HIGH fraud risk must have detailed fraud report generated.
                        Report must include fraud indicators found, similar past cases, and recommended action.
                        """,
                        Map.of("type", "compliance", "category", "APPROVAL_RULE")
                ),
                new Document("""
                        Fraud indicator — inconsistency between reported incident and supporting documents.
                        Request additional evidence if police report, medical records or photos
                        do not match claim description.
                        """,
                        Map.of("type", "compliance", "category", "FRAUD_INDICATOR")
                )
        );

        vectorStore.add(complianceRules);
        log.info("Seeded {} compliance rules", complianceRules.size());
    }
}