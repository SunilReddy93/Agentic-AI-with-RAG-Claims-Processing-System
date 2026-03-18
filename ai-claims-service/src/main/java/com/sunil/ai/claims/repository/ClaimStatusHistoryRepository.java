package com.sunil.ai.claims.repository;

import com.sunil.ai.claims.entity.ClaimStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClaimStatusHistoryRepository extends JpaRepository<ClaimStatusHistory, Long> {

        List<ClaimStatusHistory> findByClaimIdOrderByChangedAtAsc(Long claimId);

}
