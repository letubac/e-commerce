package com.ecommerce.constant;

/**
 * Coupon Module Constants
 * Error codes: E600-E649
 * Success codes: S600-S629
 */
/**
 * author: LeTuBac
 */
public class CouponConstant {

    // ============ ERROR CODES ============

    // Coupon Not Found errors (E600-E604)
    public static final String E600_COUPON_NOT_FOUND = "600_COUPON_NOT_FOUND";
    public static final String E601_COUPON_CODE_NOT_FOUND = "601_COUPON_CODE_NOT_FOUND";
    public static final String E602_COUPON_FETCH_FAILED = "602_COUPON_FETCH_FAILED";

    // Coupon Creation/Update errors (E605-E609)
    public static final String E605_COUPON_CREATION_FAILED = "605_COUPON_CREATION_FAILED";
    public static final String E606_COUPON_UPDATE_FAILED = "606_COUPON_UPDATE_FAILED";
    public static final String E607_COUPON_DELETE_FAILED = "607_COUPON_DELETE_FAILED";
    public static final String E608_COUPON_CODE_EXISTS = "608_COUPON_CODE_EXISTS";

    // Coupon Validation errors (E610-E619)
    public static final String E610_INVALID_COUPON_ID = "610_INVALID_COUPON_ID";
    public static final String E611_INVALID_COUPON_CODE = "611_INVALID_COUPON_CODE";
    public static final String E612_INVALID_DISCOUNT_VALUE = "612_INVALID_DISCOUNT_VALUE";
    public static final String E613_INVALID_MIN_ORDER_AMOUNT = "613_INVALID_MIN_ORDER_AMOUNT";
    public static final String E614_INVALID_MAX_DISCOUNT = "614_INVALID_MAX_DISCOUNT";
    public static final String E615_INVALID_USAGE_LIMIT = "615_INVALID_USAGE_LIMIT";
    public static final String E616_INVALID_DATE_RANGE = "616_INVALID_DATE_RANGE";
    public static final String E617_INVALID_DISCOUNT_TYPE = "617_INVALID_DISCOUNT_TYPE";
    public static final String E618_INVALID_ORDER_AMOUNT = "618_INVALID_ORDER_AMOUNT";

    // Coupon Usage errors (E620-E629)
    public static final String E620_COUPON_EXPIRED = "620_COUPON_EXPIRED";
    public static final String E621_COUPON_NOT_STARTED = "621_COUPON_NOT_STARTED";
    public static final String E622_COUPON_INACTIVE = "622_COUPON_INACTIVE";
    public static final String E623_COUPON_USAGE_LIMIT_REACHED = "623_COUPON_USAGE_LIMIT_REACHED";
    public static final String E624_ORDER_AMOUNT_TOO_LOW = "624_ORDER_AMOUNT_TOO_LOW";
    public static final String E625_COUPON_APPLY_FAILED = "625_COUPON_APPLY_FAILED";
    public static final String E626_COUPON_VALIDATION_FAILED = "626_COUPON_VALIDATION_FAILED";

    // Statistics errors (E630-E634)
    public static final String E630_STATISTICS_FETCH_FAILED = "630_STATISTICS_FETCH_FAILED";
    public static final String E631_COUPON_STATISTICS_FAILED = "631_COUPON_STATISTICS_FAILED"; // Toggle Status errors
                                                                                               // (E635-E639)
    public static final String E635_TOGGLE_STATUS_FAILED = "635_TOGGLE_STATUS_FAILED";
    public static final String E635_COUPON_TOGGLE_STATUS_FAILED = "635_COUPON_TOGGLE_STATUS_FAILED"; // Search errors
                                                                                                     // (E640-E644)
    public static final String E640_SEARCH_FAILED = "640_SEARCH_FAILED";
    public static final String E640_COUPON_SEARCH_FAILED = "640_COUPON_SEARCH_FAILED"; // ============ SUCCESS CODES
                                                                                       // ============

    // Coupon CRUD operations (S600-S609)
    public static final String S600_COUPON_CREATED = "S600_COUPON_CREATED";
    public static final String S601_COUPON_UPDATED = "S601_COUPON_UPDATED";
    public static final String S602_COUPON_DELETED = "S602_COUPON_DELETED";
    public static final String S603_COUPON_RETRIEVED = "S603_COUPON_RETRIEVED";
    public static final String S604_COUPONS_LISTED = "S604_COUPONS_LISTED";
    public static final String S605_ACTIVE_COUPONS_LISTED = "S605_ACTIVE_COUPONS_LISTED";

    // Coupon Validation/Apply (S610-S614)
    public static final String S610_COUPON_VALIDATED = "S610_COUPON_VALIDATED";
    public static final String S611_COUPON_APPLIED = "S611_COUPON_APPLIED";
    public static final String S612_DISCOUNT_CALCULATED = "S612_DISCOUNT_CALCULATED";
    public static final String S613_COUPON_USED = "S613_COUPON_USED";

    // Status operations (S615-S619)
    public static final String S615_STATUS_TOGGLED = "S615_STATUS_TOGGLED";
    public static final String S616_COUPON_ACTIVATED = "S616_COUPON_ACTIVATED";
    public static final String S617_COUPON_DEACTIVATED = "S617_COUPON_DEACTIVATED";

    // Statistics (S620-S624)
    public static final String S620_STATISTICS_RETRIEVED = "S620_STATISTICS_RETRIEVED";

    // Search (S625-S629)
    public static final String S625_COUPONS_SEARCHED = "S625_COUPONS_SEARCHED";

    // Private constructor to prevent instantiation
    private CouponConstant() {
        throw new IllegalStateException("Constant class cannot be instantiated");
    }
}
