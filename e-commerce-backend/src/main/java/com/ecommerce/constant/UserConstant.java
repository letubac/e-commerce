package com.ecommerce.constant;

/**
 * Constants for User module error and success codes.
 * Error codes: E950-E994
 * Success codes: S950-S994
 */
/**
 * author: LeTuBac
 */
public class UserConstant {

    // ==================== ERROR CODES ====================

    // Basic Operations (E950-E954)
    public static final String USER_NOT_FOUND = "E950_USER_NOT_FOUND";
    public static final String USER_CREATE_FAILED = "E951_USER_CREATE_FAILED";
    public static final String USER_UPDATE_FAILED = "E952_USER_UPDATE_FAILED";
    public static final String USER_DELETE_FAILED = "E953_USER_DELETE_FAILED";
    public static final String USER_FETCH_FAILED = "E954_USER_FETCH_FAILED";

    // Validation & Duplicate (E955-E959)
    public static final String USERNAME_ALREADY_EXISTS = "E955_USERNAME_ALREADY_EXISTS";
    public static final String EMAIL_ALREADY_EXISTS = "E956_EMAIL_ALREADY_EXISTS";
    public static final String INVALID_USER_DATA = "E957_INVALID_USER_DATA";
    public static final String USER_ALREADY_EXISTS = "E958_USER_ALREADY_EXISTS";

    // Password Management (E960-E964)
    public static final String PASSWORD_INVALID = "E960_PASSWORD_INVALID";
    public static final String PASSWORD_MISMATCH = "E961_PASSWORD_MISMATCH";
    public static final String PASSWORD_CHANGE_FAILED = "E962_PASSWORD_CHANGE_FAILED";
    public static final String PASSWORD_RESET_FAILED = "E963_PASSWORD_RESET_FAILED";
    public static final String PASSWORD_TOO_WEAK = "E964_PASSWORD_TOO_WEAK";

    // Account Status (E965-E969)
    public static final String ACCOUNT_LOCKED = "E965_ACCOUNT_LOCKED";
    public static final String ACCOUNT_INACTIVE = "E966_ACCOUNT_INACTIVE";
    public static final String ACCOUNT_LOCK_FAILED = "E967_ACCOUNT_LOCK_FAILED";
    public static final String ACCOUNT_UNLOCK_FAILED = "E968_ACCOUNT_UNLOCK_FAILED";
    public static final String ACCOUNT_STATUS_UPDATE_FAILED = "E969_ACCOUNT_STATUS_UPDATE_FAILED";

    // Email & Verification (E970-E974)
    public static final String EMAIL_SEND_FAILED = "E970_EMAIL_SEND_FAILED";
    public static final String EMAIL_VERIFICATION_FAILED = "E971_EMAIL_VERIFICATION_FAILED";
    public static final String EMAIL_INVALID = "E972_EMAIL_INVALID";
    public static final String VERIFICATION_TOKEN_INVALID = "E973_VERIFICATION_TOKEN_INVALID";
    public static final String VERIFICATION_TOKEN_EXPIRED = "E974_VERIFICATION_TOKEN_EXPIRED";

    // Search & Filter (E975-E979)
    public static final String USER_SEARCH_FAILED = "E975_USER_SEARCH_FAILED";
    public static final String USER_FILTER_FAILED = "E976_USER_FILTER_FAILED";

    // Statistics & Reports (E980-E984)
    public static final String USER_STATISTICS_FAILED = "E980_USER_STATISTICS_FAILED";

    // Pagination (E985-E989)
    public static final String USER_PAGINATION_FAILED = "E985_USER_PAGINATION_FAILED";

    // Authorization (E990-E994)
    public static final String USER_UNAUTHORIZED = "E990_USER_UNAUTHORIZED";
    public static final String USER_FORBIDDEN = "E991_USER_FORBIDDEN";

    // ==================== SUCCESS CODES ====================

    // Basic Operations (S950-S954)
    public static final String USER_FOUND = "S950";
    public static final String USER_CREATED = "S951";
    public static final String USER_UPDATED = "S952";
    public static final String USER_DELETED = "S953";
    public static final String USERS_RETRIEVED = "S954";

    // Validation (S955-S959)
    public static final String USERNAME_AVAILABLE = "S955";
    public static final String EMAIL_AVAILABLE = "S956";
    public static final String USER_DATA_VALID = "S957";

    // Password Management (S960-S964)
    public static final String PASSWORD_VALID = "S960";
    public static final String PASSWORD_CHANGED = "S961";
    public static final String PASSWORD_RESET_EMAIL_SENT = "S962";
    public static final String PASSWORD_RESET_SUCCESS = "S963";

    // Account Status (S965-S969)
    public static final String ACCOUNT_UNLOCKED = "S966";
    public static final String ACCOUNT_ACTIVATED = "S967";
    public static final String ACCOUNT_DEACTIVATED = "S968";

    // Email & Verification (S970-S974)
    public static final String EMAIL_SENT = "S970";
    public static final String EMAIL_VERIFIED = "S971";
    public static final String VERIFICATION_EMAIL_SENT = "S972";

    // Search & Filter (S975-S979)
    public static final String USERS_SEARCH_SUCCESS = "S975";
    public static final String USERS_FILTERED = "S976";

    // Statistics & Reports (S980-S984)
    public static final String USER_STATISTICS_RETRIEVED = "S980";

    // Pagination (S985-S989)
    public static final String USERS_PAGINATED = "S985";

    private UserConstant() {
        // Private constructor to prevent instantiation
    }
}
