package com.lecture.course.service;

import com.lecture.course.dto.CourseDto;
import com.lecture.course.entity.Course;
import com.lecture.course.kafka.CourseKafkaProducer;
import com.lecture.course.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Comparator;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseKafkaProducer kafkaProducer;

    /**
     * 종목 마스터 등록 및 security.updated 이벤트 발행
     */
    @Transactional
    public CourseDto.CourseResponse createCourse(CourseDto.CreateRequest request, Long instructorId) {
        String ticker = normalizeTicker(request.getTicker());
        if (courseRepository.existsByTickerIgnoreCase(ticker)) {
            throw new IllegalArgumentException("이미 등록된 티커입니다: " + ticker);
        }

        Course course = Course.builder()
                .ticker(ticker)
                .name(request.getName())
                .description(request.getDescription())
                .sector(request.getSector())
                .lastPrice(request.getLastPrice())
                .marketCap(request.getMarketCap())
                .exchangeId(request.getExchangeId() != null ? request.getExchangeId() : instructorId)
                .interestBearingDebt(defaultZero(request.getInterestBearingDebt()))
                .interestEarningAssets(defaultZero(request.getInterestEarningAssets()))
                .nonPermissibleIncome(defaultZero(request.getNonPermissibleIncome()))
                .totalRevenue(defaultZero(request.getTotalRevenue()))
                .build();

        Course saved = courseRepository.saveAndFlush(course);
        publishAfterCommit(saved);
        return CourseDto.CourseResponse.from(saved);
    }

    /**
     * 종목 단건 조회
     */
    public CourseDto.CourseResponse getCourse(Long id) {
        Course course = findCourseById(id);
        return CourseDto.CourseResponse.from(course);
    }

    /**
     * 전체 심사 대상 종목 조회
     */
    public List<CourseDto.CourseResponse> getAllCourses() {
        return courseRepository.findAll().stream()
                .sorted(Comparator.comparing(Course::getTicker))
                .map(CourseDto.CourseResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 업종별 COMPLIANT 종목 조회
     */
    public List<CourseDto.CourseResponse> getCoursesByCategory(Course.Sector category) {
        return courseRepository.findBySectorAndGrade(category, Course.Grade.COMPLIANT).stream()
                .map(CourseDto.CourseResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 레거시 ID 기반 종목 존재 여부 확인
     */
    public boolean existsCourse(Long id) {
        return courseRepository.existsById(id);
    }

    public CourseDto.CourseResponse getCourseByTicker(String ticker) {
        return CourseDto.CourseResponse.from(findCourseByTicker(ticker));
    }

    public CourseDto.GradeResponse getGrade(String ticker) {
        return CourseDto.GradeResponse.from(findCourseByTicker(ticker));
    }

    /**
     * 레거시 메서드명을 유지한 심사 횟수 증가
     */
    @Transactional
    public void increaseEnrollmentCount(Long courseId) {
        Course course = findCourseById(courseId);
        course.increaseEnrollmentCount();
    }

    /**
     * advisory-service용: 동일 업종 COMPLIANT 대체 종목 조회
     */
    public List<CourseDto.CourseResponse> getRecommendCourses(
            Course.Sector category, List<String> excludeCourseIds) {

        List<String> normalized = excludeCourseIds.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(this::normalizeTicker)
                .toList();

        List<Course> courses = normalized.isEmpty()
                ? courseRepository.findBySectorAndGrade(category, Course.Grade.COMPLIANT)
                : courseRepository.findBySectorAndGradeAndTickerNotIn(
                        category, Course.Grade.COMPLIANT, normalized);

        // 대체 종목은 동일 업종 COMPLIANT 중 시가총액 내림차순
        return courses.stream()
                .sorted(Comparator.comparing(
                        Course::getMarketCap,
                        Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(CourseDto.CourseResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void applyScreeningResult(String ticker, String grade, String reason, String ruleVersionId) {
        Course course = findCourseByTicker(ticker);
        course.applyScreening(Course.Grade.valueOf(grade), reason, ruleVersionId);
    }

    @Transactional
    public void flagForReview(String ticker, String reason) {
        findCourseByTicker(ticker).flagForReview(reason);
    }

    public void refreshFinancials() {
        courseRepository.findAll().forEach(this::publishAfterCommit);
    }

    private Course findCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("종목을 찾을 수 없습니다: " + id));
    }

    private Course findCourseByTicker(String ticker) {
        return courseRepository.findByTickerIgnoreCase(normalizeTicker(ticker))
                .orElseThrow(() -> new IllegalArgumentException("종목을 찾을 수 없습니다: " + ticker));
    }

    private String normalizeTicker(String ticker) {
        if (ticker == null || ticker.isBlank()) {
            throw new IllegalArgumentException("티커는 필수입니다");
        }
        return ticker.trim().toUpperCase(Locale.ROOT);
    }

    private java.math.BigDecimal defaultZero(java.math.BigDecimal value) {
        return value != null ? value : java.math.BigDecimal.ZERO;
    }

    private void publishAfterCommit(Course course) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            kafkaProducer.publishSecurityUpdated(course);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                kafkaProducer.publishSecurityUpdated(course);
            }
        });
    }
}
