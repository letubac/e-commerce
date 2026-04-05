package com.ecommerce.constant;

/**
 * Order Module Constants
 * Code Range: E750-E799 (Errors), S750-S779 (Success)
 */
/**
 * author: LeTuBac
 */
public class OrderConstant {

    // ==================== ERROR CODES ====================

    // Create Order Errors (E750-E754)
    public static final String E750_ORDER_CREATE_FAILED = "750_ORDER_CREATE_FAILED";
    public static final String E751_USER_NOT_FOUND = "751_USER_NOT_FOUND";
    public static final String E752_INSUFFICIENT_STOCK = "752_INSUFFICIENT_STOCK";
    public static final String E753_PRODUCT_NOT_FOUND_IN_ORDER = "753_PRODUCT_NOT_FOUND_IN_ORDER";
    public static final String E754_INVALID_ORDER_ITEMS = "754_INVALID_ORDER_ITEMS";

    // Get Order Errors (E755-E759)
    public static final String E755_ORDER_NOT_FOUND = "755_ORDER_NOT_FOUND";
    public static final String E756_ORDER_ACCESS_DENIED = "756_ORDER_ACCESS_DENIED";
    public static final String E757_ORDERS_FETCH_FAILED = "757_ORDERS_FETCH_FAILED";

    // Update Order Errors (E760-E769)
    public static final String E760_ORDER_UPDATE_FAILED = "760_ORDER_UPDATE_FAILED";
    public static final String E761_INVALID_ORDER_STATUS = "761_INVALID_ORDER_STATUS";
    public static final String E762_STATUS_UPDATE_NOT_ALLOWED = "762_STATUS_UPDATE_NOT_ALLOWED";
    public static final String E763_TRACKING_UPDATE_FAILED = "763_TRACKING_UPDATE_FAILED";

    // Cancel Order Errors (E770-E774)
    public static final String E770_ORDER_CANCEL_FAILED = "770_ORDER_CANCEL_FAILED";
    public static final String E771_CANCEL_NOT_ALLOWED = "771_CANCEL_NOT_ALLOWED";
    public static final String E772_STOCK_RESTORE_FAILED = "772_STOCK_RESTORE_FAILED";

    // Order Pagination/Search Errors (E775-E779)
    public static final String E775_ORDERS_BY_STATUS_FAILED = "775_ORDERS_BY_STATUS_FAILED";
    public static final String E776_ORDERS_BY_USER_FAILED = "776_ORDERS_BY_USER_FAILED";
    public static final String E777_ALL_ORDERS_FETCH_FAILED = "777_ALL_ORDERS_FETCH_FAILED";

    // Order Number Generation Errors (E780-E784)
    public static final String E780_ORDER_NUMBER_GENERATION_FAILED = "780_ORDER_NUMBER_GENERATION_FAILED";

    // ==================== SUCCESS CODES ====================

    // Create Order Success (S750-S754)
    public static final String S750_ORDER_CREATED = "S750_ORDER_CREATED";

    // Get Order Success (S755-S759)
    public static final String S755_ORDER_RETRIEVED = "S755_ORDER_RETRIEVED";
    public static final String S756_ORDERS_RETRIEVED = "S756_ORDERS_RETRIEVED";

    // Update Order Success (S760-S764)
    public static final String S760_ORDER_STATUS_UPDATED = "S760_ORDER_STATUS_UPDATED";
    public static final String S761_TRACKING_NUMBER_UPDATED = "S761_TRACKING_NUMBER_UPDATED";

    // Cancel Order Success (S765-S769)
    public static final String S765_ORDER_CANCELLED = "S765_ORDER_CANCELLED";

    // Order Number Success (S770-S774)
    public static final String S770_ORDER_NUMBER_GENERATED = "S770_ORDER_NUMBER_GENERATED";

    private OrderConstant() {
        // Private constructor to prevent instantiation
    }
}
