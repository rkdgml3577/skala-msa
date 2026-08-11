package com.lecture.payment.repository;

import com.lecture.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByTickerOrderByCreatedAtDesc(String ticker);

    List<Payment> findByOrderIdOrderByCreatedAtDesc(Long orderId);
}
