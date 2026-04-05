package com.ecommerce.constant;

/**
 * Dashboard Module Constants
 * Error codes: E700-E749
 * Success codes: S700-S729
 */
/**
 * author: LeTuBac
 */
public class DashboardConstant {

    // ============ ERROR CODES ============

    // Dashboard Overview errors (E700-E704)
    public static final String E700_DASHBOARD_OVERVIEW_FAILED = "700_DASHBOARD_OVERVIEW_FAILED";
    public static final String E701_DASHBOARD_DATA_FETCH_FAILED = "701_DASHBOARD_DATA_FETCH_FAILED";

    // Sales Statistics errors (E705-E709)
    public static final String E705_SALES_STATISTICS_FAILED = "705_SALES_STATISTICS_FAILED";
    public static final String E706_INVALID_DAYS_PARAMETER = "706_INVALID_DAYS_PARAMETER";

    // User Statistics errors (E710-E714)
    public static final String E710_USER_STATISTICS_FAILED = "710_USER_STATISTICS_FAILED";

    // Product Statistics errors (E715-E719)
    public static final String E715_PRODUCT_STATISTICS_FAILED = "715_PRODUCT_STATISTICS_FAILED";

    // Order Statistics errors (E720-E724)
    public static final String E720_ORDER_STATISTICS_FAILED = "720_ORDER_STATISTICS_FAILED";

    // Recent Activities errors (E725-E729)
    public static final String E725_ACTIVITIES_FETCH_FAILED = "725_ACTIVITIES_FETCH_FAILED";
    public static final String E726_INVALID_LIMIT_PARAMETER = "726_INVALID_LIMIT_PARAMETER";

    // System Health errors (E730-E734)
    public static final String E730_SYSTEM_HEALTH_FAILED = "730_SYSTEM_HEALTH_FAILED";

    // ============ SUCCESS CODES ============

    // Dashboard Overview success (S700-S704)
    public static final String S700_DASHBOARD_OVERVIEW_RETRIEVED = "S700_DASHBOARD_OVERVIEW_RETRIEVED";

    // Sales Statistics success (S705-S709)
    public static final String S705_SALES_STATISTICS_RETRIEVED = "S705_SALES_STATISTICS_RETRIEVED";

    // User Statistics success (S710-S714)
    public static final String S710_USER_STATISTICS_RETRIEVED = "S710_USER_STATISTICS_RETRIEVED";

    // Product Statistics success (S715-S719)
    public static final String S715_PRODUCT_STATISTICS_RETRIEVED = "S715_PRODUCT_STATISTICS_RETRIEVED";

    // Order Statistics success (S720-S724)
    public static final String S720_ORDER_STATISTICS_RETRIEVED = "S720_ORDER_STATISTICS_RETRIEVED";

    // Recent Activities success (S725-S729)
    public static final String S725_ACTIVITIES_RETRIEVED = "S725_ACTIVITIES_RETRIEVED";

    // System Health success (S730-S734)
    public static final String S730_SYSTEM_HEALTH_RETRIEVED = "S730_SYSTEM_HEALTH_RETRIEVED";

    // Private constructor to prevent instantiation
    private DashboardConstant() {
        throw new IllegalStateException("Constant class cannot be instantiated");
    }
}
