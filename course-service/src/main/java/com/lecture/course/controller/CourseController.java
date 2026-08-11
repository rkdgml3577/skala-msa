package com.lecture.course.controller;

import com.lecture.course.dto.CourseDto;
import com.lecture.course.entity.Course;
import com.lecture.course.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/courses", "/api/securities"})
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    /**
     * POST /courses - 종목 마스터 등록
     */
    @PostMapping
    public ResponseEntity<CourseDto.ApiResponse<CourseDto.CourseResponse>> createCourse(
            @Valid @RequestBody CourseDto.CreateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long instructorId) {

        CourseDto.CourseResponse response = courseService.createCourse(
                request, instructorId != null ? instructorId : 0L);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CourseDto.ApiResponse.success(response));
    }

    /**
     * GET /courses - 전체 종목 목록
     */
    @GetMapping
    public ResponseEntity<CourseDto.ApiResponse<List<CourseDto.CourseResponse>>> getAllCourses() {
        return ResponseEntity.ok(
                CourseDto.ApiResponse.success(courseService.getAllCourses())
        );
    }

    /**
     * GET /courses/{id} - 종목 상세
     */
    @GetMapping("/{id}")
    public ResponseEntity<CourseDto.ApiResponse<CourseDto.CourseResponse>> getCourse(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                CourseDto.ApiResponse.success(courseService.getCourse(id))
        );
    }

    /**
     * GET /courses/category/{category} - 업종별 적합 종목
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<CourseDto.ApiResponse<List<CourseDto.CourseResponse>>> getCoursesByCategory(
            @PathVariable Course.Sector category) {
        return ResponseEntity.ok(
                CourseDto.ApiResponse.success(courseService.getCoursesByCategory(category))
        );
    }

    /**
     * GET /courses/internal/exists/{id} - 레거시 ID 기반 종목 존재 여부
     */
    @GetMapping("/internal/exists/{id}")
    public ResponseEntity<Boolean> existsCourse(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.existsCourse(id));
    }

    /**
     * GET /courses/internal/{id} - 레거시 ID 기반 종목 상세
     */
    @GetMapping("/internal/{id}")
    public ResponseEntity<CourseDto.CourseResponse> getCourseInternal(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourse(id));
    }

    /** 종목 상세 조회 (내부 서비스용) */
    @GetMapping("/internal/ticker/{ticker}")
    public ResponseEntity<CourseDto.CourseResponse> getSecurityInternal(@PathVariable String ticker) {
        return ResponseEntity.ok(courseService.getCourseByTicker(ticker));
    }

    /** 실시간 주문 심사용 저장 등급 O(1) 조회 */
    @GetMapping("/internal/{ticker}/grade")
    public ResponseEntity<CourseDto.GradeResponse> getSecurityGrade(@PathVariable String ticker) {
        return ResponseEntity.ok(courseService.getGrade(ticker));
    }

    /**
     * POST /courses/internal/{id}/enrollment-count - 심사 횟수 증가 호환 경로
     */
    @PostMapping("/internal/{id}/enrollment-count")
    public ResponseEntity<Void> increaseEnrollmentCount(@PathVariable Long id) {
        courseService.increaseEnrollmentCount(id);
        return ResponseEntity.ok().build();
    }

    /**
     * GET /courses/internal/recommend - 동일 업종 적합 대체 종목 조회
     */
    @GetMapping("/internal/recommend")
    public ResponseEntity<List<CourseDto.CourseResponse>> getRecommendCourses(
            @RequestParam(name = "sector") Course.Sector category,
            @RequestParam(name = "excludeTickers", defaultValue = "") List<String> excludeIds) {
        return ResponseEntity.ok(courseService.getRecommendCourses(category, excludeIds));
    }

    /** 외부 목데이터 갱신/배치 심사를 즉시 재현하는 데모 엔드포인트 */
    @PostMapping("/internal/refresh")
    public ResponseEntity<Void> refreshFinancials() {
        courseService.refreshFinancials();
        return ResponseEntity.accepted().build();
    }
}
