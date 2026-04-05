package com.ecommerce.controller;

import com.ecommerce.exception.ErrorHandler;
import com.ecommerce.exception.SuccessHandler;
import com.ecommerce.service.impl.ScheduledTasksService;
import com.ecommerce.webapp.BusinessApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller for monitoring cron job statuses (Admin only).
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

    /**
     * Get the status of all registered cron jobs.
     */
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
}
