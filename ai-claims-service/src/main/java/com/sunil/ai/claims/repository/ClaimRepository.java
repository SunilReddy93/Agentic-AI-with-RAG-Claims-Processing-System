package com.sunil.ai.claims.repository;

import com.sunil.ai.claims.entity.Claim;
import com.sunil.ai.claims.enums.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClaimRepository extends JpaRepository<Claim, Long> {

    Optional<Claim> findByIdempotencyKey(String idempotencyKey);

    List<Claim> findByUserId(Long userId);

    boolean existsByUserIdAndStatus(Long userId, ClaimStatus status);
}
