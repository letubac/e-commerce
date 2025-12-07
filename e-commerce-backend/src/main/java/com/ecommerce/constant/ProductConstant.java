package com.ecommerce.constant;

/**
 * Constants for Product module error and success codes
 * Error codes: E850-E899
 * Success codes: S850-S899
 */
public class ProductConstant {

    // Error codes - Product Basic Operations (E850-E859)
    public static final String E850_PRODUCT_NOT_FOUND = "850_PRODUCT_NOT_FOUND";
    public static final String E851_PRODUCT_CREATE_FAILED = "851_PRODUCT_CREATE_FAILED";
    public static final String E852_PRODUCT_UPDATE_FAILED = "852_PRODUCT_UPDATE_FAILED";
    public static final String E853_PRODUCT_DELETE_FAILED = "853_PRODUCT_DELETE_FAILED";
    public static final String E854_PRODUCT_FETCH_FAILED = "854_PRODUCT_FETCH_FAILED";
    public static final String E855_PRODUCT_SLUG_EXISTS = "855_PRODUCT_SLUG_EXISTS";
    public static final String E856_PRODUCT_SKU_EXISTS = "856_PRODUCT_SKU_EXISTS";
    public static final String E857_INVALID_PRODUCT_DATA = "857_INVALID_PRODUCT_DATA";

    // Error codes - Product Status Operations (E860-E864)
    public static final String E860_PRODUCT_STATUS_UPDATE_FAILED = "860_PRODUCT_STATUS_UPDATE_FAILED";
    public static final String E861_PRODUCT_ACTIVATE_FAILED = "861_PRODUCT_ACTIVATE_FAILED";
    public static final String E862_PRODUCT_DEACTIVATE_FAILED = "862_PRODUCT_DEACTIVATE_FAILED";
    public static final String E863_PRODUCT_ALREADY_ACTIVE = "863_PRODUCT_ALREADY_ACTIVE";
    public static final String E864_PRODUCT_ALREADY_INACTIVE = "864_PRODUCT_ALREADY_INACTIVE";

    // Error codes - Product Stock Operations (E865-E869)
    public static final String E865_PRODUCT_STOCK_UPDATE_FAILED = "865_PRODUCT_STOCK_UPDATE_FAILED";
    public static final String E866_INSUFFICIENT_STOCK = "866_INSUFFICIENT_STOCK";
    public static final String E867_INVALID_STOCK_QUANTITY = "867_INVALID_STOCK_QUANTITY";

    // Error codes - Product Search & Filter (E870-E874)
    public static final String E870_PRODUCT_SEARCH_FAILED = "870_PRODUCT_SEARCH_FAILED";
    public static final String E871_INVALID_SEARCH_KEYWORD = "871_INVALID_SEARCH_KEYWORD";
    public static final String E872_INVALID_FILTER_PARAMS = "872_INVALID_FILTER_PARAMS";

    // Error codes - Product Category & Brand (E875-E879)
    public static final String E875_PRODUCTS_BY_CATEGORY_FAILED = "875_PRODUCTS_BY_CATEGORY_FAILED";
    public static final String E876_PRODUCTS_BY_BRAND_FAILED = "876_PRODUCTS_BY_BRAND_FAILED";
    public static final String E877_INVALID_CATEGORY_ID = "877_INVALID_CATEGORY_ID";
    public static final String E878_INVALID_BRAND_ID = "878_INVALID_BRAND_ID";

    // Error codes - Product Price (E880-E884)
    public static final String E880_INVALID_PRICE_RANGE = "880_INVALID_PRICE_RANGE";
    public static final String E881_PRICE_UPDATE_FAILED = "881_PRICE_UPDATE_FAILED";

    // Success codes - Product Basic Operations (S850-S859)
    public static final String S850_PRODUCT_RETRIEVED = "S850_PRODUCT_RETRIEVED";
    public static final String S851_PRODUCT_CREATED = "S851_PRODUCT_CREATED";
    public static final String S852_PRODUCT_UPDATED = "S852_PRODUCT_UPDATED";
    public static final String S853_PRODUCT_DELETED = "S853_PRODUCT_DELETED";
    public static final String S854_PRODUCTS_RETRIEVED = "S854_PRODUCTS_RETRIEVED";

    // Success codes - Product Status Operations (S860-S864)
    public static final String S860_PRODUCT_STATUS_UPDATED = "S860_PRODUCT_STATUS_UPDATED";
    public static final String S861_PRODUCT_ACTIVATED = "S861_PRODUCT_ACTIVATED";
    public static final String S862_PRODUCT_DEACTIVATED = "S862_PRODUCT_DEACTIVATED";

    // Success codes - Product Stock Operations (S865-S869)
    public static final String S865_PRODUCT_STOCK_UPDATED = "S865_PRODUCT_STOCK_UPDATED";

    // Success codes - Product Search & Filter (S870-S874)
    public static final String S870_PRODUCT_SEARCH_SUCCESS = "S870_PRODUCT_SEARCH_SUCCESS";
    public static final String S871_SEARCH_SUGGESTIONS_RETRIEVED = "S871_SEARCH_SUGGESTIONS_RETRIEVED";

    // Success codes - Product Category & Brand (S875-S879)
    public static final String S875_PRODUCTS_BY_CATEGORY_RETRIEVED = "S875_PRODUCTS_BY_CATEGORY_RETRIEVED";
    public static final String S876_PRODUCTS_BY_BRAND_RETRIEVED = "S876_PRODUCTS_BY_BRAND_RETRIEVED";

    private ProductConstant() {
        // Private constructor to prevent instantiation
    }
}
