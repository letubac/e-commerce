package com.ecommerce.exception;

/**
 * author: LeTuBac
 */
public enum ErrorHint {
    REQUIRE_LOGIN,            // Redirect login
    REQUIRE_CREATE_ACCOUNT,      // Redirect create
    DENY_APPLICATION,      // Reason deny application
    DUPLICATE_INTERVIEW,
    NO_APPLICATION,
    ;
    public static ErrorHint getByValue(String value) {
        for (ErrorHint questionType : ErrorHint.values()) {
            if (questionType.name().equalsIgnoreCase(value)) {
                return questionType;
            }
        }
        return null;
    }
}
