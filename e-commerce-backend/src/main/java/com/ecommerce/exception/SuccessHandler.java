package com.ecommerce.exception;

import com.ecommerce.webapp.BusinessApiResponse;

/**
 * author: LeTuBac
 */
public interface SuccessHandler {

	public BusinessApiResponse handlerSuccess(Object data, long start);

    public BusinessApiResponse handlerSuccessAdmin(Object data);
   
    
    public BusinessApiResponse handlerSuccess(Object data, Integer statusCode ,long start);
}
