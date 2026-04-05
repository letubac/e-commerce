package com.ecommerce.scheduler;

import com.ecommerce.service.FlashSaleService;
import com.ecommerce.service.impl.ScheduledTasksService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * author: LeTuBac
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FlashSaleScheduler {

    static final String JOB_NAME = "syncFlashSaleStatus";

    private final FlashSaleService flashSaleService;
    private final ScheduledTasksService scheduledTasksService;

    /**
     * Runs every 10 seconds to:
     * 1. Auto-activate flash sales when startTime arrives (if they have products)
     * 2. Auto-expire flash sales when endTime passes
     * 3. Mark sold-out products as inactive within active flash sales
     *
     * Respects isJobActive() so admin can disable/pause via CronJobController.
     */
    @Scheduled(fixedRate = 10000)
    public void syncFlashSaleStatus() {
        if (!scheduledTasksService.isJobActive(JOB_NAME)) {
            log.debug("[FlashSaleScheduler] Job '{}' is disabled/paused — skipping", JOB_NAME);
            return;
        }
        try {
            flashSaleService.syncFlashSaleStatus();
            scheduledTasksService.recordJobSuccess(JOB_NAME, "Flash sale sync completed");
        } catch (Exception e) {
            log.error("[FlashSaleScheduler] Error during sync: {}", e.getMessage(), e);
            scheduledTasksService.recordJobError(JOB_NAME, e.getMessage());
        }
    }
}
