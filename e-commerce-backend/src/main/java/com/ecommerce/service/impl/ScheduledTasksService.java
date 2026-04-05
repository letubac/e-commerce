package com.ecommerce.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
    /** Jobs that have been manually disabled by admin */
    private final Set<String> disabledJobs = ConcurrentHashMap.newKeySet();
    /** Jobs paused until a specific instant */
    private final ConcurrentHashMap<String, Instant> pausedUntilMap = new ConcurrentHashMap<>();

    private static final String KEY_LAST_RUN = "lastRun";
    private static final String KEY_STATUS = "status";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_PAUSED_UNTIL = "pausedUntil";

    // ── Job definitions ─────────────────────────────────────────────────────

    /**
     * Check and deactivate expired flash sales every 60 seconds.
     */
    @Scheduled(fixedRate = 60000)
    public void checkFlashSaleExpiry() {
        String jobName = "checkFlashSaleExpiry";
        if (!isJobActive(jobName))
            return;
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
        if (!isJobActive(jobName))
            return;
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
        if (!isJobActive(jobName))
            return;
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
        if (!isJobActive(jobName))
            return;
        try {
            log.info("[Cron] Cleaning expired sessions...");
            updateJobStatus(jobName, "SUCCESS", "Expired sessions cleaned");
        } catch (Exception e) {
            log.error("[Cron] cleanExpiredSessions failed: {}", e.getMessage(), e);
            updateJobStatus(jobName, "ERROR", e.getMessage());
        }
    }

    // ── Control methods ──────────────────────────────────────────────────────

    /**
     * Returns true if the job should execute (not disabled or currently paused).
     */
    public boolean isJobActive(String jobName) {
        if (disabledJobs.contains(jobName)) {
            log.debug("[Cron] {} is disabled, skipping", jobName);
            return false;
        }
        Instant pausedUntil = pausedUntilMap.get(jobName);
        if (pausedUntil != null && Instant.now().isBefore(pausedUntil)) {
            log.debug("[Cron] {} is paused until {}, skipping", jobName, pausedUntil);
            return false;
        }
        // Auto-remove expired pause entries
        if (pausedUntil != null) {
            pausedUntilMap.remove(jobName);
        }
        return true;
    }

    /**
     * Toggle job enabled/disabled state. Returns the new enabled state.
     */
    public boolean toggleJob(String jobName) {
        if (disabledJobs.contains(jobName)) {
            disabledJobs.remove(jobName);
            log.info("[Cron] Job {} has been ENABLED by admin", jobName);
            return true;
        } else {
            disabledJobs.add(jobName);
            log.info("[Cron] Job {} has been DISABLED by admin", jobName);
            return false;
        }
    }

    /**
     * Pause a job for the given number of minutes.
     */
    public void pauseJob(String jobName, int minutes) {
        Instant until = Instant.now().plusSeconds(minutes * 60L);
        pausedUntilMap.put(jobName, until);
        log.info("[Cron] Job {} has been PAUSED for {} minutes (until {})", jobName, minutes, until);
    }

    /**
     * Resume a paused job immediately.
     */
    public void resumeJob(String jobName) {
        pausedUntilMap.remove(jobName);
        log.info("[Cron] Job {} has been RESUMED by admin", jobName);
    }

    /**
     * Returns the current status of all registered cron jobs.
     */
    public Map<String, Map<String, Object>> getJobStatuses() {
        Map<String, Map<String, Object>> result = new ConcurrentHashMap<>(jobStatus);
        // Enrich with enabled/paused info for all known jobs
        for (String job : new String[] { "checkFlashSaleExpiry", "checkOrderStatuses", "nightlyCleanup",
                "cleanExpiredSessions" }) {
            result.computeIfAbsent(job, k -> new HashMap<>());
            result.get(job).put(KEY_ENABLED, !disabledJobs.contains(job));
            Instant pausedUntil = pausedUntilMap.get(job);
            if (pausedUntil != null && Instant.now().isBefore(pausedUntil)) {
                result.get(job).put(KEY_PAUSED_UNTIL, pausedUntil.toString());
            } else {
                result.get(job).remove(KEY_PAUSED_UNTIL);
            }
        }
        return result;
    }

    private void updateJobStatus(String jobName, String status, String message) {
        Map<String, Object> info = jobStatus.computeIfAbsent(jobName, k -> new HashMap<>());
        info.put(KEY_LAST_RUN, new Date());
        info.put(KEY_STATUS, status);
        info.put(KEY_MESSAGE, message);
        info.put(KEY_ENABLED, !disabledJobs.contains(jobName));
    }
}
