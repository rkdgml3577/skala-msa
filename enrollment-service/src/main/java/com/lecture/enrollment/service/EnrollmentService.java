package com.lecture.enrollment.service;

import com.lecture.enrollment.dto.EnrollmentDto;
import com.lecture.enrollment.entity.Enrollment;
import com.lecture.enrollment.kafka.EnrollmentKafkaProducer;
import com.lecture.enrollment.kafka.KafkaEvent;
import com.lecture.enrollment.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseServiceClient courseServiceClient;
    private final PaymentServiceClient paymentServiceClient;
    private final EnrollmentKafkaProducer kafkaProducer;
    private final EnrollmentWriteService enrollmentWriteService;

    /**
     * 매매주문 전체 흐름: PENDING 저장 → order.received → 저장 등급 조회 → 최종 판정
     */
    public EnrollmentDto.EnrollmentResponse placeOrder(Long clientId, EnrollmentDto.EnrollRequest request) {
        String ticker = request.getTicker().trim().toUpperCase(Locale.ROOT);

        Enrollment enrollment = enrollmentWriteService.createPendingEnrollment(
                clientId, ticker, request.getSide(), request.getQuantity(), request.getOrderPrice());

        kafkaProducer.publishEnrollmentCompleted(
                KafkaEvent.OrderReceivedEvent.builder()
                        .orderId(enrollment.getId())
                        .clientId(clientId)
                        .ticker(ticker)
                        .side(request.getSide().name())
                        .quantity(request.getQuantity())
                        .orderPrice(request.getOrderPrice())
                        .build());

        CourseServiceClient.GradeResult grade = courseServiceClient.getGrade(ticker);
        Enrollment.Status status = statusFor(grade.getGrade());
        List<String> alternatives = status == Enrollment.Status.HELD
                ? paymentServiceClient.requestAlternatives(ticker)
                : List.of();

        Enrollment completed = enrollmentWriteService.applyScreeningResult(
                enrollment.getId(), status, grade.getReason(), alternatives);
        log.info("[EnrollmentService] 실시간 주문 판정 - orderId: {}, ticker: {}, status: {}",
                completed.getId(), ticker, status);
        return EnrollmentDto.EnrollmentResponse.from(completed);
    }

    /** 기존 Enrollment 저장소를 재사용한 은행사별 탐지 시나리오 구독. */
    public EnrollmentDto.EnrollmentResponse subscribe(Long clientId, EnrollmentDto.SubscriptionRequest request) {
        String ticker = request.getTicker().trim().toUpperCase(Locale.ROOT);
        Enrollment existing = enrollmentRepository
                .findFirstByClientIdAndTickerAndStatusInOrderByCreatedAtDesc(
                        clientId, ticker, List.of(Enrollment.Status.ACTIVE, Enrollment.Status.PAUSED))
                .orElse(null);
        if (existing != null) {
            return EnrollmentDto.EnrollmentResponse.from(existing);
        }
        Enrollment enrollment = enrollmentWriteService.createSubscription(clientId, ticker);
        return EnrollmentDto.EnrollmentResponse.from(enrollment);
    }

    public EnrollmentDto.EnrollmentResponse changeSubscriptionStatus(
            Long clientId, Long subscriptionId, Enrollment.Status status) {
        Enrollment enrollment = enrollmentWriteService.changeSubscriptionStatus(subscriptionId, clientId, status);
        return EnrollmentDto.EnrollmentResponse.from(enrollment);
    }

    public List<EnrollmentDto.EnrollmentResponse> getSubscriptionsByUser(Long userId) {
        return enrollmentRepository.findByClientIdAndStatusInOrderByCreatedAtDesc(
                        userId,
                        List.of(Enrollment.Status.ACTIVE, Enrollment.Status.PAUSED, Enrollment.Status.CANCELLED))
                .stream()
                .map(EnrollmentDto.EnrollmentResponse::from)
                .toList();
    }

    /**
     * 비동기 screening.completed 결과의 멱등 반영
     */
    public void applyScreeningResult(Long orderId, String ticker, String grade, String reason) {
        Enrollment.Status status = statusFor(grade);
        List<String> alternatives = status == Enrollment.Status.HELD
                ? paymentServiceClient.requestAlternatives(ticker)
                : List.of();
        enrollmentWriteService.applyScreeningResult(orderId, status, reason, alternatives);
    }

    /**
     * 고객사 주문 목록에 security-service의 종목 요약을 조립
     */
    public List<EnrollmentDto.EnrollmentResponse> getEnrollmentsByUser(Long userId) {
        List<Enrollment> enrollments = enrollmentRepository.findByClientIdOrderByCreatedAtDesc(userId);

        return enrollments.stream()
                .map(enrollment -> {
                    Map<String, Object> courseInfo = courseServiceClient.getSecurity(enrollment.getTicker());

                    EnrollmentDto.CourseSummary courseSummary = EnrollmentDto.CourseSummary.builder()
                            .ticker((String) courseInfo.get("ticker"))
                            .name((String) courseInfo.get("name"))
                            .description((String) courseInfo.get("description"))
                            .sector((String) courseInfo.get("sector"))
                            .lastPrice(toBigDecimal(courseInfo.get("lastPrice")))
                            .marketCap(toBigDecimal(courseInfo.get("marketCap")))
                            .grade((String) courseInfo.get("grade"))
                            .build();

                    return EnrollmentDto.EnrollmentResponse.from(enrollment, courseSummary);
                })
                .collect(Collectors.toList());
    }

    /**
     * 보유/보류 티커 이력 조회 - advisory-service용
     */
    public EnrollmentDto.EnrollmentHistoryResponse getEnrollmentHistory(Long userId) {
        List<String> approvedTickers = enrollmentRepository
                .findByClientIdAndStatusIn(userId, List.of(Enrollment.Status.APPROVED, Enrollment.Status.WARNED))
                .stream()
                .map(Enrollment::getTicker)
                .distinct()
                .collect(Collectors.toList());

        List<String> heldTickers = enrollmentRepository
                .findByClientIdAndStatus(userId, Enrollment.Status.HELD)
                .stream()
                .map(Enrollment::getTicker)
                .distinct()
                .collect(Collectors.toList());

        return EnrollmentDto.EnrollmentHistoryResponse.builder()
                .clientId(userId)
                .approvedTickers(approvedTickers)
                .heldTickers(heldTickers)
                .build();
    }

    private Enrollment.Status statusFor(String grade) {
        return switch (grade) {
            case "COMPLIANT" -> Enrollment.Status.APPROVED;
            case "WATCH" -> Enrollment.Status.WARNED;
            case "RESTRICTED" -> Enrollment.Status.HELD;
            default -> throw new IllegalArgumentException("지원하지 않는 종목 등급입니다: " + grade);
        };
    }

    private java.math.BigDecimal toBigDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof java.math.BigDecimal decimal) return decimal;
        return new java.math.BigDecimal(value.toString());
    }

}
