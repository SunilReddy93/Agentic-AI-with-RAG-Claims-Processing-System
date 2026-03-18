package com.sunil.ai.claims.repository;

import com.sunil.ai.claims.entity.AiFraudAssessment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiFraudAssessmentRepository extends JpaRepository<AiFraudAssessment, Long> {
}
