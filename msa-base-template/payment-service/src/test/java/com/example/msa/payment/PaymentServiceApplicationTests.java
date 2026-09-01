package com.example.msa.payment;

import com.example.msa.payment.entity.Payment;
import com.example.msa.payment.kafka.PaymentEventProducer;
import com.example.msa.payment.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@SpringBootTest
class PaymentServiceApplicationTests {

    @Autowired
    private PaymentService paymentService;

    @MockBean
    private PaymentEventProducer paymentEventProducer;

    @Test
    void contextLoads() {
    }

    @Test
    void approvesOrderWithinThreshold() {
        Payment payment = paymentService.processOrder(1L, 1L, 10L, new BigDecimal("500000"));
        assertEquals(Payment.Status.APPROVED, payment.getStatus());
        verify(paymentEventProducer).publishPaymentCompleted(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void declinesOrderOverThreshold() {
        Payment payment = paymentService.processOrder(2L, 1L, 10L, new BigDecimal("5000000"));
        assertEquals(Payment.Status.DECLINED, payment.getStatus());
    }
}
