package com.ecommerce.constant;

/**
 * Payment Module Constants
 * Code Range: E800-E849 (Errors), S800-S829 (Success)
 */
/**
 * author: LeTuBac
 */
public class PaymentConstant {

    // ==================== ERROR CODES ====================

    // Payment Methods Errors (E800-E804)
    public static final String E800_PAYMENT_METHODS_FETCH_FAILED = "800_PAYMENT_METHODS_FETCH_FAILED";
    public static final String E801_PAYMENT_METHOD_NOT_FOUND = "801_PAYMENT_METHOD_NOT_FOUND";
    public static final String E802_PAYMENT_METHOD_DISABLED = "802_PAYMENT_METHOD_DISABLED";

    // Process Payment Errors (E805-E814)
    public static final String E805_PAYMENT_PROCESS_FAILED = "805_PAYMENT_PROCESS_FAILED";
    public static final String E806_INVALID_PAYMENT_METHOD = "806_INVALID_PAYMENT_METHOD";
    public static final String E807_INVALID_PAYMENT_AMOUNT = "807_INVALID_PAYMENT_AMOUNT";
    public static final String E808_INVALID_ORDER_ID = "808_INVALID_ORDER_ID";
    public static final String E809_PAYMENT_GATEWAY_ERROR = "809_PAYMENT_GATEWAY_ERROR";
    public static final String E810_INSUFFICIENT_FUNDS = "810_INSUFFICIENT_FUNDS";

    // Verify Payment Errors (E815-E819)
    public static final String E815_PAYMENT_VERIFICATION_FAILED = "815_PAYMENT_VERIFICATION_FAILED";
    public static final String E816_INVALID_TRANSACTION_ID = "816_INVALID_TRANSACTION_ID";
    public static final String E817_TRANSACTION_NOT_FOUND = "817_TRANSACTION_NOT_FOUND";

    // Payment Status Errors (E820-E824)
    public static final String E820_PAYMENT_STATUS_FETCH_FAILED = "820_PAYMENT_STATUS_FETCH_FAILED";
    public static final String E821_PAYMENT_HISTORY_FETCH_FAILED = "821_PAYMENT_HISTORY_FETCH_FAILED";

    // Payment Statistics Errors (E825-E829)
    public static final String E825_PAYMENT_STATISTICS_FAILED = "825_PAYMENT_STATISTICS_FAILED";

    // Refund Errors (E830-E839)
    public static final String E830_REFUND_CREATE_FAILED = "830_REFUND_CREATE_FAILED";
    public static final String E831_REFUND_NOT_ALLOWED = "831_REFUND_NOT_ALLOWED";
    public static final String E832_INVALID_REFUND_AMOUNT = "832_INVALID_REFUND_AMOUNT";
    public static final String E833_REFUND_STATUS_FETCH_FAILED = "833_REFUND_STATUS_FETCH_FAILED";
    public static final String E834_REFUND_NOT_FOUND = "834_REFUND_NOT_FOUND";

    // ==================== SUCCESS CODES ====================

    // Payment Methods Success (S800-S804)
    public static final String S800_PAYMENT_METHODS_RETRIEVED = "S800_PAYMENT_METHODS_RETRIEVED";

    // Process Payment Success (S805-S809)
    public static final String S805_PAYMENT_PROCESSED = "S805_PAYMENT_PROCESSED";

    // Verify Payment Success (S810-S814)
    public static final String S810_PAYMENT_VERIFIED = "S810_PAYMENT_VERIFIED";

    // Payment Status Success (S815-S819)
    public static final String S815_PAYMENT_STATUS_RETRIEVED = "S815_PAYMENT_STATUS_RETRIEVED";
    public static final String S816_PAYMENT_HISTORY_RETRIEVED = "S816_PAYMENT_HISTORY_RETRIEVED";

    // Payment Statistics Success (S820-S824)
    public static final String S820_PAYMENT_STATISTICS_RETRIEVED = "S820_PAYMENT_STATISTICS_RETRIEVED";

    // Refund Success (S825-S829)
    public static final String S825_REFUND_CREATED = "S825_REFUND_CREATED";
    public static final String S826_REFUND_STATUS_RETRIEVED = "S826_REFUND_STATUS_RETRIEVED";

    private PaymentConstant() {
        // Private constructor to prevent instantiation
    }
}
