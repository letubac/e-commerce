package com.ecommerce.constant;

/**
 * Constants for Flash Sale module error and success codes
 * Error codes: E700-E749
 * Success codes: S700-S749
 */
/**
 * author: LeTuBac
 */
public class FlashSaleConstant {
    // ==================== ERROR CODES ====================

    // Basic Operations (E700-E704)
    public static final String E700_FLASH_SALE_NOT_FOUND = "E700_FLASH_SALE_NOT_FOUND";
    public static final String E701_FLASH_SALE_CREATE_FAILED = "E701_FLASH_SALE_CREATE_FAILED";
    public static final String E702_FLASH_SALE_UPDATE_FAILED = "E702_FLASH_SALE_UPDATE_FAILED";
    public static final String E703_FLASH_SALE_DELETE_FAILED = "E703_FLASH_SALE_DELETE_FAILED";
    public static final String E704_FLASH_SALE_FETCH_FAILED = "E704_FLASH_SALE_FETCH_FAILED";

    // Validation & Duplicate (E705-E709)
    public static final String E705_FLASH_SALE_NAME_ALREADY_EXISTS = "E705_FLASH_SALE_NAME_ALREADY_EXISTS";
    public static final String E706_FLASH_SALE_INVALID_DATA = "E706_FLASH_SALE_INVALID_DATA";
    public static final String E707_FLASH_SALE_ALREADY_EXISTS = "E707_FLASH_SALE_ALREADY_EXISTS";
    public static final String E708_FLASH_SALE_TIME_OVERLAP = "E708_FLASH_SALE_TIME_OVERLAP";
    public static final String E709_FLASH_SALE_INVALID_TIME_RANGE = "E709_FLASH_SALE_INVALID_TIME_RANGE";

    // Flash Sale Status (E710-E714)
    public static final String E710_FLASH_SALE_ALREADY_STARTED = "E710_FLASH_SALE_ALREADY_STARTED";
    public static final String E711_FLASH_SALE_ALREADY_ENDED = "E711_FLASH_SALE_ALREADY_ENDED";
    public static final String E712_FLASH_SALE_NOT_ACTIVE = "E712_FLASH_SALE_NOT_ACTIVE";
    public static final String E713_FLASH_SALE_ACTIVATE_FAILED = "E713_FLASH_SALE_ACTIVATE_FAILED";
    public static final String E714_FLASH_SALE_DEACTIVATE_FAILED = "E714_FLASH_SALE_DEACTIVATE_FAILED";

    // Flash Sale Product (E715-E724)
    public static final String E715_FLASH_SALE_PRODUCT_NOT_FOUND = "E715_FLASH_SALE_PRODUCT_NOT_FOUND";
    public static final String E716_FLASH_SALE_PRODUCT_CREATE_FAILED = "E716_FLASH_SALE_PRODUCT_CREATE_FAILED";
    public static final String E717_FLASH_SALE_PRODUCT_UPDATE_FAILED = "E717_FLASH_SALE_PRODUCT_UPDATE_FAILED";
    public static final String E718_FLASH_SALE_PRODUCT_DELETE_FAILED = "E718_FLASH_SALE_PRODUCT_DELETE_FAILED";
    public static final String E719_FLASH_SALE_PRODUCT_ALREADY_EXISTS = "E719_FLASH_SALE_PRODUCT_ALREADY_EXISTS";
    public static final String E720_FLASH_SALE_PRODUCT_SOLD_OUT = "E720_FLASH_SALE_PRODUCT_SOLD_OUT";
    public static final String E721_FLASH_SALE_PRODUCT_INVALID_PRICE = "E721_FLASH_SALE_PRODUCT_INVALID_PRICE";
    public static final String E722_FLASH_SALE_PRODUCT_INVALID_STOCK = "E722_FLASH_SALE_PRODUCT_INVALID_STOCK";
    public static final String E723_FLASH_SALE_PRODUCT_LIMIT_EXCEEDED = "E723_FLASH_SALE_PRODUCT_LIMIT_EXCEEDED";
    public static final String E724_FLASH_SALE_PRODUCT_NOT_AVAILABLE = "E724_FLASH_SALE_PRODUCT_NOT_AVAILABLE";

    // Statistics & Search (E725-E729)
    public static final String E725_FLASH_SALE_SEARCH_FAILED = "E725_FLASH_SALE_SEARCH_FAILED";
    public static final String E726_FLASH_SALE_STATISTICS_FAILED = "E726_FLASH_SALE_STATISTICS_FAILED";

    // Pagination (E730-E734)
    public static final String E730_FLASH_SALE_PAGINATION_FAILED = "E730_FLASH_SALE_PAGINATION_FAILED";

    // Authorization (E735-E739)
    public static final String E735_FLASH_SALE_UNAUTHORIZED = "E735_FLASH_SALE_UNAUTHORIZED";

    // ==================== SUCCESS CODES ====================

    // Basic Operations (S700-S704)
    public static final String S700_FLASH_SALE_FOUND = "S700_FLASH_SALE_FOUND";
    public static final String S701_FLASH_SALE_CREATED = "S701_FLASH_SALE_CREATED";
    public static final String S702_FLASH_SALE_UPDATED = "S702_FLASH_SALE_UPDATED";
    public static final String S703_FLASH_SALE_DELETED = "S703_FLASH_SALE_DELETED";
    public static final String S704_FLASH_SALES_LISTED = "S704_FLASH_SALES_LISTED";

    // Flash Sale Status (S710-S714)
    public static final String S710_FLASH_SALE_ACTIVATED = "S710_FLASH_SALE_ACTIVATED";
    public static final String S711_FLASH_SALE_DEACTIVATED = "S711_FLASH_SALE_DEACTIVATED";
    public static final String S712_FLASH_SALE_STATUS_UPDATED = "S712_FLASH_SALE_STATUS_UPDATED";
    public static final String S713_ACTIVE_FLASH_SALE_FOUND = "S713_ACTIVE_FLASH_SALE_FOUND";
    public static final String S714_UPCOMING_FLASH_SALES_LISTED = "S714_UPCOMING_FLASH_SALES_LISTED";

    // Flash Sale Product (S715-S724)
    public static final String S715_FLASH_SALE_PRODUCT_FOUND = "S715_FLASH_SALE_PRODUCT_FOUND";
    public static final String S716_FLASH_SALE_PRODUCT_CREATED = "S716_FLASH_SALE_PRODUCT_CREATED";
    public static final String S717_FLASH_SALE_PRODUCT_UPDATED = "S717_FLASH_SALE_PRODUCT_UPDATED";
    public static final String S718_FLASH_SALE_PRODUCT_DELETED = "S718_FLASH_SALE_PRODUCT_DELETED";
    public static final String S719_FLASH_SALE_PRODUCTS_LISTED = "S719_FLASH_SALE_PRODUCTS_LISTED";
    public static final String S720_FLASH_SALE_PRODUCTS_ADDED = "S720_FLASH_SALE_PRODUCTS_ADDED";
    public static final String S721_FLASH_SALE_PRODUCTS_REMOVED = "S721_FLASH_SALE_PRODUCTS_REMOVED";
    public static final String S722_FLASH_SALE_PRODUCT_STOCK_UPDATED = "S722_FLASH_SALE_PRODUCT_STOCK_UPDATED";

    // Statistics (S725-S729)
    public static final String S725_FLASH_SALE_STATISTICS_FETCHED = "S725_FLASH_SALE_STATISTICS_FETCHED";

    // Pagination (S730-S734)
    public static final String S730_FLASH_SALES_PAGE_FETCHED = "S730_FLASH_SALES_PAGE_FETCHED";
}
