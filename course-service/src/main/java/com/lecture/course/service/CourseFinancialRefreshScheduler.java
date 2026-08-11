package com.lecture.course.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "screening.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class CourseFinancialRefreshScheduler {

    private final CourseService courseService;

    @Scheduled(cron = "${screening.scheduler.cron:0 0 2 * * *}", zone = "Asia/Seoul")
    public void refreshFinancials() {
        log.info("[Scheduler] 목 재무데이터 기반 전 종목 재심사를 요청합니다.");
        courseService.refreshFinancials();
    }
}
