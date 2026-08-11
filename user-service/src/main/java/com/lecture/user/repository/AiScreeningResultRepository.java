package com.lecture.user.repository;

import com.lecture.user.entity.AiScreeningResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AiScreeningResultRepository extends JpaRepository<AiScreeningResult, Long> {
    List<AiScreeningResult> findTop100ByClientIdOrderByCreatedAtDesc(Long clientId);

    List<AiScreeningResult> findByClientIdAndTransactionIdAndCreatedAtGreaterThanEqualOrderByCreatedAtAsc(
            Long clientId, String transactionId, LocalDateTime createdAt);
}
