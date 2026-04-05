package com.ecommerce.controller;

import com.ecommerce.exception.ErrorHandler;
import com.ecommerce.exception.SuccessHandler;
import com.ecommerce.service.impl.ScheduledTasksService;
import com.ecommerce.webapp.BusinessApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for monitoring and controlling cron job execution (Admin
 * only).
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/cron-jobs")
@RequiredArgsConstructor
/**
 * author: LeTuBac
 */
public class CronJobController {

    private final ScheduledTasksService scheduledTasksService;
    private final ErrorHandler errorHandler;
    private final SuccessHandler successHandler;

    /** Get the status of all registered cron jobs. */
    @GetMapping
    public ResponseEntity<BusinessApiResponse> getCronJobStatuses() {
        long start = System.currentTimeMillis();
        try {
            Map<String, Map<String, Object>> statuses = scheduledTasksService.getJobStatuses();
            return ResponseEntity.ok(successHandler.handlerSuccess(statuses, start));
        } catch (Exception e) {
            log.error("Error fetching cron job statuses", e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /** Toggle a job enabled/disabled. Returns new enabled state. */
    @PutMapping("/{jobName}/toggle")
    public ResponseEntity<BusinessApiResponse> toggleCronJob(@PathVariable("jobName") String jobName) {
        long start = System.currentTimeMillis();
        try {
            boolean enabled = scheduledTasksService.toggleJob(jobName);
            Map<String, Object> result = Map.of("jobName", jobName, "enabled", enabled);
            log.info("Admin toggled cron job '{}': enabled={}", jobName, enabled);
            return ResponseEntity.ok(successHandler.handlerSuccess(result, start));
        } catch (Exception e) {
            log.error("Error toggling cron job '{}'", jobName, e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /** Pause a job for the given number of minutes (default 60). */
    @PutMapping("/{jobName}/pause")
    public ResponseEntity<BusinessApiResponse> pauseCronJob(
            @PathVariable("jobName") String jobName,
            @RequestParam(defaultValue = "60") int minutes) {
        long start = System.currentTimeMillis();
        try {
            if (minutes <= 0 || minutes > 1440) {
                throw new IllegalArgumentException("Thời gian tạm ngưng phải từ 1 đến 1440 phút");
            }
            scheduledTasksService.pauseJob(jobName, minutes);
            Map<String, Object> result = Map.of("jobName", jobName, "pausedMinutes", minutes);
            log.info("Admin paused cron job '{}' for {} minutes", jobName, minutes);
            return ResponseEntity.ok(successHandler.handlerSuccess(result, start));
        } catch (Exception e) {
            log.error("Error pausing cron job '{}'", jobName, e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }

    /** Resume a paused job immediately. */
    @PutMapping("/{jobName}/resume")
    public ResponseEntity<BusinessApiResponse> resumeCronJob(@PathVariable("jobName") String jobName) {
        long start = System.currentTimeMillis();
        try {
            scheduledTasksService.resumeJob(jobName);
            Map<String, Object> result = Map.of("jobName", jobName, "resumed", true);
            log.info("Admin resumed cron job '{}'", jobName);
            return ResponseEntity.ok(successHandler.handlerSuccess(result, start));
        } catch (Exception e) {
            log.error("Error resuming cron job '{}'", jobName, e);
            return ResponseEntity.ok(errorHandler.handlerException(e, start));
        }
    }
}
