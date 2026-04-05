package com.ecommerce.exception;

/**
 * author: LeTuBac
 */
public class SystemException extends RuntimeException {

    /** Serial version.*/
    private static final long serialVersionUID = -4430803840375325775L;

    public SystemException(String message, Throwable cause) {
        super(message, cause);
    }

    public SystemException(String message) {
        super(message);
    }

    public SystemException(Throwable cause) {
        super(cause);
    }
}

