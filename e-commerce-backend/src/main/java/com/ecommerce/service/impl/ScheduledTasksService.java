package com.ecommerce.service.impl;

import com.ecommerce.entity.Order;
import com.ecommerce.repository.CouponRepository;
import com.ecommerce.repository.CronJobConfigRepository;
import com.ecommerce.repository.NotificationRepository;
import com.ecommerce.repository.OrderRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service for scheduled/cron jobs in the e-commerce backend.
 * All jobs check {@link #isJobActive(String)} before executing — admins can
 * disable or pause any job via {@code CronJobController} without restarting
 * the application.
 */
@Slf4j
@Service
@RequiredArgsConstructor
/**
 * author: LeTuBac
 */
public class ScheduledTasksService {

    private final OrderRepository orderRepository;
    private final NotificationRepository notificationRepository;
    private final CouponRepository couponRepository;
    private final CronJobConfigRepository cronJobConfigRepository;

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

    /** All registered job keys (used for status enrichment). */
    private static final String[] ALL_JOB_KEYS = {
            "syncFlashSaleStatus",
            "checkFlashSaleExpiry",
            "checkOrderStatuses",
            "nightlyCleanup",
            "cleanExpiredSessions"
    };

    // ── Startup ──────────────────────────────────────────────────────────────

    /**
     * On startup, restore disabled-job state from the DB so that admin
     * preferences survive server restarts.
     */
    @PostConstruct
    public void loadJobConfigsFromDb() {
        try {
            cronJobConfigRepository.findAll().forEach(config -> {
                if (!config.isEnabled()) {
                    disabledJobs.add(config.getJobName());
                    log.info("[Cron] Job '{}' loaded as DISABLED from DB", config.getJobName());
                }
            });
        } catch (Exception e) {
            log.warn("[Cron] Could not load job configs from DB (table may not exist yet): {}", e.getMessage());
        }
    }

    // ── Job definitions ──────────────────────────────────────────────────────

    /**
     * Deactivate expired / fully-used coupons every 60 seconds.
     * Flash-sale activate/expire logic is handled by {@code FlashSaleScheduler}.
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkFlashSaleExpiry() {
        String jobName = "checkFlashSaleExpiry";
        if (!isJobActive(jobName))
            return;
        try {
            Date now = new Date();
            int deactivated = couponRepository.deactivateExpiredCoupons(now);
            String msg = "Deactivated " + deactivated + " expired/fully-used coupon(s)";
            if (deactivated > 0) {
                log.info("[Cron:{}] {}", jobName, msg);
            }
            recordJobSuccess(jobName, msg);
        } catch (Exception e) {
            log.error("[Cron:{}] failed: {}", jobName, e.getMessage(), e);
            recordJobError(jobName, e.getMessage());
        }
    }

    /**
     * Check for stale PENDING orders every 5 minutes.
     * Orders PENDING for more than 48 h → log warning.
     * Orders PENDING for more than 7 days → auto-cancel with system reason.
     */
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void checkOrderStatuses() {
        String jobName = "checkOrderStatuses";
        if (!isJobActive(jobName))
            return;
        try {
            Date now = new Date();
            Date twoDaysAgo = daysAgo(now, 2);
            Date sevenDaysAgo = daysAgo(now, 7);

            // Orders between 2 and 7 days old → warning
            List<Order> warnOrders = orderRepository.findByCreatedAtBetween(sevenDaysAgo, twoDaysAgo)
                    .stream()
                    .filter(o -> "PENDING".equals(o.getStatus()))
                    .collect(Collectors.toList());

            if (!warnOrders.isEmpty()) {
                log.warn("[Cron:{}] {} PENDING order(s) older than 48h — IDs: {}",
                        jobName, warnOrders.size(),
                        warnOrders.stream().map(o -> o.getId().toString()).collect(Collectors.joining(", ")));
            }

            // Orders older than 7 days → auto-cancel
            Date fourteenDaysAgo = daysAgo(now, 14);
            List<Order> cancelOrders = orderRepository.findByCreatedAtBetween(fourteenDaysAgo, sevenDaysAgo)
                    .stream()
                    .filter(o -> "PENDING".equals(o.getStatus()))
                    .collect(Collectors.toList());

            int cancelled = 0;
            for (Order order : cancelOrders) {
                order.setStatus("CANCELLED");
                order.setCancelledAt(now);
                order.setCancellationReason(
                        "T\u1ef1 \u0111\u1ed9ng h\u1ee7y: \u0111\u01a1n h\u00e0ng PENDING qu\u00e1 7 ng\u00e0y kh\u00f4ng \u0111\u01b0\u1ee3c x\u1eed l\u00fd");
                order.setUpdatedAt(now);
                orderRepository.updateOrder(order);
                cancelled++;
                log.info("[Cron:{}] Auto-cancelled order id={} (pending > 7 days)", jobName, order.getId());
            }

            String msg = String.format("%d warning(s), %d auto-cancelled", warnOrders.size(), cancelled);
            recordJobSuccess(jobName, msg);
        } catch (Exception e) {
            log.error("[Cron:{}] failed: {}", jobName, e.getMessage(), e);
            recordJobError(jobName, e.getMessage());
        }
    }

    /**
     * Nightly cleanup at 02:00 AM every day.
     * - Delete notifications older than 90 days.
     * - Delete expired notifications (expires_at < NOW()).
     * - Deactivate expired/fully-used coupons (safety net alongside
     * checkFlashSaleExpiry).
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void nightlyCleanup() {
        String jobName = "nightlyCleanup";
        if (!isJobActive(jobName))
            return;
        try {
            Date now = new Date();

            // 1. Delete notifications older than 90 days
            Date ninetyDaysAgo = daysAgo(now, 90);
            int oldDeleted = notificationRepository.deleteOldNotifications(ninetyDaysAgo);

            // 2. Delete notifications with expires_at < now
            int expiredDeleted = notificationRepository.deleteExpiredNotifications(now);

            // 3. Deactivate expired / fully-used coupons
            int couponsDeactivated = couponRepository.deactivateExpiredCoupons(now);

            String msg = String.format(
                    "Deleted %d old notifications, %d expired notifications; deactivated %d coupon(s)",
                    oldDeleted, expiredDeleted, couponsDeactivated);
            log.info("[Cron:{}] {}", jobName, msg);
            recordJobSuccess(jobName, msg);
        } catch (Exception e) {
            log.error("[Cron:{}] failed: {}", jobName, e.getMessage(), e);
            recordJobError(jobName, e.getMessage());
        }
    }

    /**
     * Hourly cleanup for short-lived / time-boxed notifications (expires_at <
     * NOW()).
     */
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cleanExpiredSessions() {
        String jobName = "cleanExpiredSessions";
        if (!isJobActive(jobName))
            return;
        try {
            Date now = new Date();
            int deleted = notificationRepository.deleteExpiredNotifications(now);
            String msg = "Deleted " + deleted + " expired notification(s)";
            if (deleted > 0) {
                log.info("[Cron:{}] {}", jobName, msg);
            }
            recordJobSuccess(jobName, msg);
        } catch (Exception e) {
            log.error("[Cron:{}] failed: {}", jobName, e.getMessage(), e);
            recordJobError(jobName, e.getMessage());
        }
    }

    // ── Control methods ──────────────────────────────────────────────────────

    /**
     * Returns true if the job should execute (not disabled and not currently
     * paused).
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
        boolean isNowEnabled;
        if (disabledJobs.contains(jobName)) {
            disabledJobs.remove(jobName);
            isNowEnabled = true;
            log.info("[Cron] Job {} has been ENABLED by admin", jobName);
        } else {
            disabledJobs.add(jobName);
            isNowEnabled = false;
            log.info("[Cron] Job {} has been DISABLED by admin", jobName);
        }
        try {
            cronJobConfigRepository.upsert(jobName, isNowEnabled, new Date());
        } catch (Exception e) {
            log.error("[Cron] Failed to persist job config for '{}': {}", jobName, e.getMessage());
        }
        return isNowEnabled;
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
     * Called by {@code FlashSaleScheduler} (and any other external scheduler)
     * to record a successful run into the shared status map.
     */
    public void recordJobSuccess(String jobName, String message) {
        updateJobStatus(jobName, "SUCCESS", message);
    }

    /**
     * Called by external schedulers to record a failed run.
     */
    public void recordJobError(String jobName, String message) {
        updateJobStatus(jobName, "ERROR", message);
    }

    /**
     * Returns the current status of all registered cron jobs.
     */
    public Map<String, Map<String, Object>> getJobStatuses() {
        Map<String, Map<String, Object>> result = new ConcurrentHashMap<>(jobStatus);
        for (String job : ALL_JOB_KEYS) {
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

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void updateJobStatus(String jobName, String status, String message) {
        Map<String, Object> info = jobStatus.computeIfAbsent(jobName, k -> new HashMap<>());
        info.put(KEY_LAST_RUN, new Date());
        info.put(KEY_STATUS, status);
        info.put(KEY_MESSAGE, message);
        info.put(KEY_ENABLED, !disabledJobs.contains(jobName));
    }

    /** Returns a Date that is {@code days} days before {@code from}. */
    private static Date daysAgo(Date from, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(from);
        cal.add(Calendar.DAY_OF_YEAR, -days);
        return cal.getTime();
    }
}
