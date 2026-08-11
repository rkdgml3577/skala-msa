package com.lecture.enrollment.repository;

import com.lecture.enrollment.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByClientIdOrderByCreatedAtDesc(Long clientId);

    List<Enrollment> findByClientIdAndStatus(Long clientId, Enrollment.Status status);

    Optional<Enrollment> findFirstByClientIdAndTickerOrderByCreatedAtDesc(Long clientId, String ticker);

    Optional<Enrollment> findFirstByClientIdAndTickerAndStatusInOrderByCreatedAtDesc(
            Long clientId, String ticker, List<Enrollment.Status> statuses);

    Optional<Enrollment> findByIdAndClientId(Long id, Long clientId);

    boolean existsByClientIdAndTickerAndStatus(Long clientId, String ticker, Enrollment.Status status);

    List<Enrollment> findByClientIdAndStatusIn(Long clientId, List<Enrollment.Status> statuses);

    List<Enrollment> findByClientIdAndStatusInOrderByCreatedAtDesc(Long clientId, List<Enrollment.Status> statuses);
}
