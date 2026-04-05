package com.ecommerce.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * author: LeTuBac
 */
public class DateUtils {

    public static Date getStartOfMonth() {
        LocalDateTime ldt = LocalDate.now()
                .withDayOfMonth(1)
                .atStartOfDay();
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date minusDays(int days) {
        LocalDateTime ldt = LocalDateTime.now().minusDays(days);
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date minusHours(int hours) {
        LocalDateTime ldt = LocalDateTime.now().minusHours(hours);
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date minusMonths(int months, Date baseDate) {
        LocalDateTime ldt = LocalDateTime
                .ofInstant(baseDate.toInstant(), ZoneId.systemDefault())
                .minusMonths(months);
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }
}