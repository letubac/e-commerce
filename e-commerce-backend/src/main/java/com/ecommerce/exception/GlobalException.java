package com.ecommerce.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
/**
 * author: LeTuBac
 */
public class GlobalException extends Exception{

    private static final long serialVersionUID = 2392926148247590423L;

    protected ExceptionCode exceptionCode;
}
