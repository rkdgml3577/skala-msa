package com.example.msa.course.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** @Scheduled 배치 작업 예시. 주기적으로 등록된 상품 수를 로깅한다. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ItemCountScheduler {

    private final ItemService itemService;

    @Scheduled(fixedDelayString = "${scheduler.item-count.fixed-delay:60000}")
    public void logItemCount() {
        log.info("[Scheduler] 현재 등록 상품 수: {}", itemService.count());
    }
}
