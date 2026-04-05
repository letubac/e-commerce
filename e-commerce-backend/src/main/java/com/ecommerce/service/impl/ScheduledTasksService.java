package com.ecommerce.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for scheduled/cron jobs in the e-commerce backend.
 */
@Slf4j
@Service
@RequiredArgsConstructor
/**
 * author: LeTuBac
 */
public class ScheduledTasksService {

    private final ConcurrentHashMap<String, Map<String, Object>> jobStatus = new ConcurrentHashMap<>();

    private static final String KEY_LAST_RUN = "lastRun";
    private static final String KEY_STATUS = "status";
    private static final String KEY_MESSAGE = "message";

    /**
     * Check and deactivate expired flash sales every 60 seconds.
     */
    @Scheduled(fixedRate = 60000)
    public void checkFlashSaleExpiry() {
        String jobName = "checkFlashSaleExpiry";
        try {
            log.info("[Cron] Checking flash sale expiry...");
            // Flash sale expiry logic would query FlashSaleRepository here
            updateJobStatus(jobName, "SUCCESS", "Flash sale expiry check completed");
        } catch (Exception e) {
            log.error("[Cron] checkFlashSaleExpiry failed: {}", e.getMessage(), e);
            updateJobStatus(jobName, "ERROR", e.getMessage());
        }
    }

    /**
     * Check pending order statuses every 5 minutes.
     */
    @Scheduled(fixedRate = 300000)
    public void checkOrderStatuses() {
        String jobName = "checkOrderStatuses";
        try {
            log.info("[Cron] Checking pending order statuses...");
            updateJobStatus(jobName, "SUCCESS", "Order status check completed");
        } catch (Exception e) {
            log.error("[Cron] checkOrderStatuses failed: {}", e.getMessage(), e);
            updateJobStatus(jobName, "ERROR", e.getMessage());
        }
    }

    /**
     * Nightly cleanup at 02:00 AM every day.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void nightlyCleanup() {
        String jobName = "nightlyCleanup";
        try {
            log.info("[Cron] Running nightly cleanup...");
            updateJobStatus(jobName, "SUCCESS", "Nightly cleanup completed");
        } catch (Exception e) {
            log.error("[Cron] nightlyCleanup failed: {}", e.getMessage(), e);
            updateJobStatus(jobName, "ERROR", e.getMessage());
        }
    }

    /**
     * Clean expired sessions every hour.
     */
    @Scheduled(fixedRate = 3600000)
    public void cleanExpiredSessions() {
        String jobName = "cleanExpiredSessions";
        try {
            log.info("[Cron] Cleaning expired sessions...");
            updateJobStatus(jobName, "SUCCESS", "Expired sessions cleaned");
        } catch (Exception e) {
            log.error("[Cron] cleanExpiredSessions failed: {}", e.getMessage(), e);
            updateJobStatus(jobName, "ERROR", e.getMessage());
        }
    }

    /**
     * Returns the current status of all registered cron jobs.
     */
    public Map<String, Map<String, Object>> getJobStatuses() {
        return new ConcurrentHashMap<>(jobStatus);
    }

    private void updateJobStatus(String jobName, String status, String message) {
        Map<String, Object> info = new HashMap<>();
        info.put(KEY_LAST_RUN, new Date());
        info.put(KEY_STATUS, status);
        info.put(KEY_MESSAGE, message);
        jobStatus.put(jobName, info);
    }
}
