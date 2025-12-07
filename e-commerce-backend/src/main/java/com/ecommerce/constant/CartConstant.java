package com.ecommerce.constant;

/**
 * Cart Module Constants
 * Error codes: E300-E349
 * Success codes: S300-S329
 */
public class CartConstant {

    // ============ ERROR CODES ============

    // Cart errors (E300-E309)
    public static final String E300_CART_NOT_FOUND = "300_CART_NOT_FOUND";
    public static final String E301_CART_CREATE_ERROR = "301_CART_CREATE_ERROR";
    public static final String E302_CART_UPDATE_ERROR = "302_CART_UPDATE_ERROR";
    public static final String E303_CART_DELETE_ERROR = "303_CART_DELETE_ERROR";
    public static final String E304_CART_GET_ERROR = "304_CART_GET_ERROR";
    public static final String E305_CART_CLEAR_ERROR = "305_CART_CLEAR_ERROR";
    public static final String E306_ADMIN_NO_CART = "306_ADMIN_NO_CART";

    // Cart item errors (E310-E319)
    public static final String E310_CART_ITEM_NOT_FOUND = "310_CART_ITEM_NOT_FOUND";
    public static final String E311_CART_ITEM_ADD_ERROR = "311_CART_ITEM_ADD_ERROR";
    public static final String E312_CART_ITEM_UPDATE_ERROR = "312_CART_ITEM_UPDATE_ERROR";
    public static final String E313_CART_ITEM_DELETE_ERROR = "313_CART_ITEM_DELETE_ERROR";
    public static final String E314_CART_ITEM_QUANTITY_INVALID = "314_CART_ITEM_QUANTITY_INVALID";

    // Product/Stock validation errors (E320-E329)
    public static final String E320_PRODUCT_NOT_FOUND = "320_PRODUCT_NOT_FOUND";
    public static final String E321_PRODUCT_OUT_OF_STOCK = "321_PRODUCT_OUT_OF_STOCK";
    public static final String E322_INSUFFICIENT_STOCK = "322_INSUFFICIENT_STOCK";
    public static final String E323_PRODUCT_INACTIVE = "323_PRODUCT_INACTIVE";
    public static final String E324_PRODUCT_PRICE_CHANGED = "324_PRODUCT_PRICE_CHANGED";

    // User validation errors (E330-E339)
    public static final String E330_USER_NOT_FOUND = "330_USER_NOT_FOUND";
    public static final String E331_USER_INACTIVE = "331_USER_INACTIVE";
    public static final String E332_USER_NOT_CUSTOMER = "332_USER_NOT_CUSTOMER";

    // Business logic errors (E340-E349)
    public static final String E340_CART_EMPTY = "340_CART_EMPTY";
    public static final String E341_CART_TOTAL_CALCULATION_ERROR = "341_CART_TOTAL_CALCULATION_ERROR";
    public static final String E342_CART_MERGE_ERROR = "342_CART_MERGE_ERROR";
    public static final String E343_DUPLICATE_CART_ITEM = "343_DUPLICATE_CART_ITEM";

    // ============ SUCCESS CODES ============

    // Cart operations (S300-S309)
    public static final String S300_CART_RETRIEVED = "S300_CART_RETRIEVED";
    public static final String S301_CART_CREATED = "S301_CART_CREATED";
    public static final String S302_CART_UPDATED = "S302_CART_UPDATED";
    public static final String S303_CART_CLEARED = "S303_CART_CLEARED";

    // Cart item operations (S310-S319)
    public static final String S310_ITEM_ADDED = "S310_ITEM_ADDED";
    public static final String S311_ITEM_UPDATED = "S311_ITEM_UPDATED";
    public static final String S312_ITEM_REMOVED = "S312_ITEM_REMOVED";
    public static final String S313_ITEM_QUANTITY_UPDATED = "S313_ITEM_QUANTITY_UPDATED";

    // Calculation operations (S320-S329)
    public static final String S320_TOTAL_CALCULATED = "S320_TOTAL_CALCULATED";

    private CartConstant() {
        // Private constructor to prevent instantiation
    }
}
