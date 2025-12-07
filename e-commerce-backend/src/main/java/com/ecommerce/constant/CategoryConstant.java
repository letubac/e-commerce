package com.ecommerce.constant;

/**
 * Constants for Category module error and success codes
 * Error codes: E400-E449
 * Success codes: S400-S429
 */
public class CategoryConstant {

    // ============================================
    // ERROR CODES (E400-E449)
    // ============================================

    // Category Not Found Errors (E400-E404)
    public static final String E400_CATEGORY_NOT_FOUND = "400_CATEGORY_NOT_FOUND";
    public static final String E401_CATEGORY_NOT_EXIST = "401_CATEGORY_NOT_EXIST";

    // Category Operation Errors (E405-E409)
    public static final String E405_CATEGORY_GET_ERROR = "405_CATEGORY_GET_ERROR";
    public static final String E406_CATEGORY_CREATE_ERROR = "406_CATEGORY_CREATE_ERROR";
    public static final String E407_CATEGORY_UPDATE_ERROR = "407_CATEGORY_UPDATE_ERROR";
    public static final String E408_CATEGORY_DELETE_ERROR = "408_CATEGORY_DELETE_ERROR";
    public static final String E409_CATEGORY_TOGGLE_ERROR = "409_CATEGORY_TOGGLE_ERROR";

    // Category Validation Errors (E410-E419)
    public static final String E410_CATEGORY_NAME_REQUIRED = "410_CATEGORY_NAME_REQUIRED";
    public static final String E411_CATEGORY_NAME_EXISTS = "411_CATEGORY_NAME_EXISTS";
    public static final String E412_CATEGORY_NAME_TOO_LONG = "412_CATEGORY_NAME_TOO_LONG";
    public static final String E413_CATEGORY_INVALID_DATA = "413_CATEGORY_INVALID_DATA";
    public static final String E414_CATEGORY_ID_REQUIRED = "414_CATEGORY_ID_REQUIRED";
    public static final String E415_CATEGORY_SLUG_EXISTS = "415_CATEGORY_SLUG_EXISTS";

    // Category Parent/Child Errors (E420-E424)
    public static final String E420_PARENT_CATEGORY_NOT_FOUND = "420_PARENT_CATEGORY_NOT_FOUND";
    public static final String E421_CIRCULAR_PARENT_REFERENCE = "421_CIRCULAR_PARENT_REFERENCE";
    public static final String E422_INVALID_PARENT_CATEGORY = "422_INVALID_PARENT_CATEGORY";

    // Category Product Association Errors (E425-E429)
    public static final String E425_CATEGORY_HAS_PRODUCTS = "425_CATEGORY_HAS_PRODUCTS";
    public static final String E426_CATEGORY_HAS_CHILDREN = "426_CATEGORY_HAS_CHILDREN";
    public static final String E427_CANNOT_DELETE_ACTIVE_CATEGORY = "427_CANNOT_DELETE_ACTIVE_CATEGORY";

    // Category Search/Pagination Errors (E430-E434)
    public static final String E430_CATEGORY_SEARCH_ERROR = "430_CATEGORY_SEARCH_ERROR";
    public static final String E431_CATEGORY_PAGINATION_ERROR = "431_CATEGORY_PAGINATION_ERROR";

    // Category Image Errors (E435-E439)
    public static final String E435_CATEGORY_IMAGE_UPLOAD_ERROR = "435_CATEGORY_IMAGE_UPLOAD_ERROR";
    public static final String E436_CATEGORY_IMAGE_DELETE_ERROR = "436_CATEGORY_IMAGE_DELETE_ERROR";
    public static final String E437_CATEGORY_IMAGE_INVALID = "437_CATEGORY_IMAGE_INVALID";

    // General Category Errors (E440-E449)
    public static final String E440_CATEGORY_OPERATION_FAILED = "440_CATEGORY_OPERATION_FAILED";
    public static final String E441_CATEGORY_ACCESS_DENIED = "441_CATEGORY_ACCESS_DENIED";
    public static final String E442_CATEGORY_DUPLICATE = "442_CATEGORY_DUPLICATE";

    // ============================================
    // SUCCESS CODES (S400-S429)
    // ============================================

    // Category Retrieval Success (S400-S404)
    public static final String S400_CATEGORY_FOUND = "S400_CATEGORY_FOUND";
    public static final String S401_CATEGORIES_LISTED = "S401_CATEGORIES_LISTED";
    public static final String S402_ACTIVE_CATEGORIES_LISTED = "S402_ACTIVE_CATEGORIES_LISTED";
    public static final String S403_CATEGORY_DETAILS_RETRIEVED = "S403_CATEGORY_DETAILS_RETRIEVED";

    // Category CRUD Success (S405-S414)
    public static final String S405_CATEGORY_CREATED = "S405_CATEGORY_CREATED";
    public static final String S406_CATEGORY_UPDATED = "S406_CATEGORY_UPDATED";
    public static final String S407_CATEGORY_DELETED = "S407_CATEGORY_DELETED";
    public static final String S408_CATEGORY_STATUS_TOGGLED = "S408_CATEGORY_STATUS_TOGGLED";
    public static final String S409_CATEGORY_ACTIVATED = "S409_CATEGORY_ACTIVATED";
    public static final String S410_CATEGORY_DEACTIVATED = "S410_CATEGORY_DEACTIVATED";

    // Category Search/Pagination Success (S415-S419)
    public static final String S415_CATEGORY_SEARCH_SUCCESS = "S415_CATEGORY_SEARCH_SUCCESS";
    public static final String S416_CATEGORY_PAGE_RETRIEVED = "S416_CATEGORY_PAGE_RETRIEVED";

    // Category Product Association Success (S420-S424)
    public static final String S420_CATEGORY_PRODUCTS_LISTED = "S420_CATEGORY_PRODUCTS_LISTED";
    public static final String S421_CATEGORY_PRODUCT_COUNT = "S421_CATEGORY_PRODUCT_COUNT";

    // Category Image Success (S425-S429)
    public static final String S425_CATEGORY_IMAGE_UPLOADED = "S425_CATEGORY_IMAGE_UPLOADED";
    public static final String S426_CATEGORY_IMAGE_UPDATED = "S426_CATEGORY_IMAGE_UPDATED";
    public static final String S427_CATEGORY_IMAGE_DELETED = "S427_CATEGORY_IMAGE_DELETED";

    // Private constructor to prevent instantiation
    private CategoryConstant() {
        throw new IllegalStateException("Constant class cannot be instantiated");
    }
}
