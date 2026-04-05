package com.ecommerce.constant;

/**
 * Constants for Brand module error and success codes.
 * Error codes: 200-249
 * Success codes: S200-S219
 */
/**
 * author: LeTuBac
 */
public final class BrandConstant {

    private BrandConstant() {
        throw new UnsupportedOperationException("This is a constant class and cannot be instantiated");
    }

    // ===========================
    // ERROR CODES (200-249)
    // ===========================

    // Brand CRUD Errors (200-219)
    public static final String E200_BRAND_NOT_FOUND = "200_BRAND_NOT_FOUND";
    public static final String E201_BRAND_CREATE_ERROR = "201_BRAND_CREATE_ERROR";
    public static final String E202_BRAND_UPDATE_ERROR = "202_BRAND_UPDATE_ERROR";
    public static final String E203_BRAND_DELETE_ERROR = "203_BRAND_DELETE_ERROR";
    public static final String E204_BRAND_LIST_ERROR = "204_BRAND_LIST_ERROR";
    public static final String E205_BRAND_NAME_EXISTS = "205_BRAND_NAME_EXISTS";
    public static final String E206_BRAND_NAME_REQUIRED = "206_BRAND_NAME_REQUIRED";
    public static final String E207_BRAND_INVALID_DATA = "207_BRAND_INVALID_DATA";
    public static final String E208_BRAND_HAS_PRODUCTS = "208_BRAND_HAS_PRODUCTS";
    public static final String E209_BRAND_SLUG_EXISTS = "209_BRAND_SLUG_EXISTS";

    // Brand Status Errors (210-219)
    public static final String E210_BRAND_TOGGLE_STATUS_ERROR = "210_BRAND_TOGGLE_STATUS_ERROR";
    public static final String E211_BRAND_ALREADY_ACTIVE = "211_BRAND_ALREADY_ACTIVE";
    public static final String E212_BRAND_ALREADY_INACTIVE = "212_BRAND_ALREADY_INACTIVE";

    // Brand Search/Statistics Errors (220-229)
    public static final String E220_BRAND_SEARCH_ERROR = "220_BRAND_SEARCH_ERROR";
    public static final String E221_BRAND_STATISTICS_ERROR = "221_BRAND_STATISTICS_ERROR";
    public static final String E222_BRAND_PRODUCTS_ERROR = "222_BRAND_PRODUCTS_ERROR";

    // Brand Image/URL Errors (230-239)
    public static final String E230_BRAND_IMAGE_UPLOAD_ERROR = "230_BRAND_IMAGE_UPLOAD_ERROR";
    public static final String E231_BRAND_IMAGE_DELETE_ERROR = "231_BRAND_IMAGE_DELETE_ERROR";
    public static final String E232_BRAND_INVALID_URL = "232_BRAND_INVALID_URL";
    public static final String E233_BRAND_IMAGE_SIZE_EXCEEDED = "233_BRAND_IMAGE_SIZE_EXCEEDED";
    public static final String E234_BRAND_IMAGE_TYPE_INVALID = "234_BRAND_IMAGE_TYPE_INVALID";

    // ===========================
    // SUCCESS CODES (S200-S219)
    // ===========================

    // Brand CRUD Success (S200-S209)
    public static final String S200_BRAND_CREATED = "S200_BRAND_CREATED";
    public static final String S201_BRAND_UPDATED = "S201_BRAND_UPDATED";
    public static final String S202_BRAND_DELETED = "S202_BRAND_DELETED";
    public static final String S203_BRAND_FOUND = "S203_BRAND_FOUND";
    public static final String S204_BRAND_LIST_SUCCESS = "S204_BRAND_LIST_SUCCESS";

    // Brand Status Success (S210-S214)
    public static final String S210_BRAND_STATUS_TOGGLED = "S210_BRAND_STATUS_TOGGLED";
    public static final String S211_BRAND_ACTIVATED = "S211_BRAND_ACTIVATED";
    public static final String S212_BRAND_DEACTIVATED = "S212_BRAND_DEACTIVATED";

    // Brand Search/Statistics Success (S215-S219)
    public static final String S215_BRAND_SEARCH_SUCCESS = "S215_BRAND_SEARCH_SUCCESS";
    public static final String S216_BRAND_STATISTICS_SUCCESS = "S216_BRAND_STATISTICS_SUCCESS";
    public static final String S217_BRAND_PRODUCTS_SUCCESS = "S217_BRAND_PRODUCTS_SUCCESS";
    public static final String S218_BRAND_IMAGE_UPLOADED = "S218_BRAND_IMAGE_UPLOADED";
    public static final String S219_BRAND_IMAGE_DELETED = "S219_BRAND_IMAGE_DELETED";
}
