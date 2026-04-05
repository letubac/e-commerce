package com.ecommerce.constant;

/**
 * author: LeTuBac
 */
public class ReviewConstant {

    // Error codes (E900-E949)

    // Review Basic Operations (E900-E909)
    public static final String E900_REVIEW_NOT_FOUND = "900_REVIEW_NOT_FOUND";
    public static final String E901_REVIEW_CREATE_FAILED = "901_REVIEW_CREATE_FAILED";
    public static final String E902_REVIEW_UPDATE_FAILED = "902_REVIEW_UPDATE_FAILED";
    public static final String E903_REVIEW_DELETE_FAILED = "903_REVIEW_DELETE_FAILED";
    public static final String E904_REVIEW_FETCH_FAILED = "904_REVIEW_FETCH_FAILED";
    public static final String E905_REVIEW_ALREADY_EXISTS = "905_REVIEW_ALREADY_EXISTS";
    public static final String E906_REVIEW_INVALID_DATA = "906_REVIEW_INVALID_DATA";

    // Review Permissions (E910-E914)
    public static final String E910_REVIEW_UNAUTHORIZED = "910_REVIEW_UNAUTHORIZED";
    public static final String E911_REVIEW_NOT_OWNED = "911_REVIEW_NOT_OWNED";
    public static final String E912_REVIEW_CANNOT_REVIEW_OWN_PRODUCT = "912_REVIEW_CANNOT_REVIEW_OWN_PRODUCT";

    // Review Rating Operations (E915-E919)
    public static final String E915_INVALID_RATING = "915_INVALID_RATING";
    public static final String E916_RATING_CALCULATION_FAILED = "916_RATING_CALCULATION_FAILED";

    // Review Product Operations (E920-E924)
    public static final String E920_PRODUCT_NOT_FOUND = "920_PRODUCT_NOT_FOUND";
    public static final String E921_PRODUCT_REVIEWS_FETCH_FAILED = "921_PRODUCT_REVIEWS_FETCH_FAILED";
    public static final String E922_PRODUCT_NOT_PURCHASED = "922_PRODUCT_NOT_PURCHASED";

    // Review User Operations (E925-E929)
    public static final String E925_USER_NOT_FOUND = "925_USER_NOT_FOUND";
    public static final String E926_USER_REVIEWS_FETCH_FAILED = "926_USER_REVIEWS_FETCH_FAILED";

    // Review Status Operations (E930-E934)
    public static final String E930_STATUS_UPDATE_FAILED = "930_STATUS_UPDATE_FAILED";
    public static final String E931_INVALID_STATUS = "931_INVALID_STATUS";

    // Review Statistics (E935-E939)
    public static final String E935_STATISTICS_FETCH_FAILED = "935_STATISTICS_FETCH_FAILED";
    public static final String E936_SUMMARY_FETCH_FAILED = "936_SUMMARY_FETCH_FAILED";

    // Review Search & Filter (E940-E944)
    public static final String E940_SEARCH_FAILED = "940_SEARCH_FAILED";
    public static final String E941_FILTER_FAILED = "941_FILTER_FAILED";

    // Success codes (S900-S949)

    // Review Basic Operations (S900-S909)
    public static final String S900_REVIEW_RETRIEVED = "S900_REVIEW_RETRIEVED";
    public static final String S901_REVIEW_CREATED = "S901_REVIEW_CREATED";
    public static final String S902_REVIEW_UPDATED = "S902_REVIEW_UPDATED";
    public static final String S903_REVIEW_DELETED = "S903_REVIEW_DELETED";
    public static final String S904_REVIEWS_RETRIEVED = "S904_REVIEWS_RETRIEVED";

    // Review Rating Operations (S915-S919)
    public static final String S915_RATING_RETRIEVED = "S915_RATING_RETRIEVED";
    public static final String S916_RATING_COUNT_RETRIEVED = "S916_RATING_COUNT_RETRIEVED";

    // Review Product Operations (S920-S924)
    public static final String S920_PRODUCT_REVIEWS_RETRIEVED = "S920_PRODUCT_REVIEWS_RETRIEVED";
    public static final String S921_PRODUCT_REVIEW_SUMMARY_RETRIEVED = "S921_PRODUCT_REVIEW_SUMMARY_RETRIEVED";

    // Review User Operations (S925-S929)
    public static final String S925_USER_REVIEWS_RETRIEVED = "S925_USER_REVIEWS_RETRIEVED";
    public static final String S926_USER_CAN_REVIEW = "S926_USER_CAN_REVIEW";

    // Review Status Operations (S930-S934)
    public static final String S930_STATUS_UPDATED = "S930_STATUS_UPDATED";

    // Review Statistics (S935-S939)
    public static final String S935_STATISTICS_RETRIEVED = "S935_STATISTICS_RETRIEVED";

    // Review Admin Operations (S940-S944)
    public static final String S940_ADMIN_REVIEWS_RETRIEVED = "S940_ADMIN_REVIEWS_RETRIEVED";
    public static final String S941_ADMIN_REVIEW_DELETED = "S941_ADMIN_REVIEW_DELETED";

    private ReviewConstant() {
        // Private constructor to prevent instantiation
    }
}
