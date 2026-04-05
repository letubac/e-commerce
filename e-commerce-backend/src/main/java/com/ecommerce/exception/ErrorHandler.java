package com.ecommerce.exception;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.BindingResult;

import com.ecommerce.webapp.BusinessApiResponse;

@Qualifier("errorHandler")
/**
 * author: LeTuBac
 */
public interface ErrorHandler {

	public BusinessApiResponse handlerException(Exception ex, long start);

    public BusinessApiResponse handlerException(int codeStatus,String message);
    
    public BusinessApiResponse handlerBindingResult(BindingResult bindingResult, long start);
}
