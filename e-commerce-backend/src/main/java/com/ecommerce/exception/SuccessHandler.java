package com.ecommerce.exception;

import com.ecommerce.webapp.BusinessApiResponse;

public interface SuccessHandler {

	public BusinessApiResponse handlerSuccess(Object data, long start);

    public BusinessApiResponse handlerSuccessAdmin(Object data);
   
    
    public BusinessApiResponse handlerSuccess(Object data, Integer statusCode ,long start);
}
