package com.ecommerce.exception.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.ecommerce.constant.AppCoreConstant;
import com.ecommerce.exception.SuccessHandler;
import com.ecommerce.webapp.BusinessApiResponse;

/**
 * SuccessHandlerApiImpl
 * 
 * @version 01-00
 * @since 01-00
 * @author BacLV
 */
@Component
/**
 * author: LeTuBac
 */
public class SuccessHandlerApiImpl implements SuccessHandler  {

    public BusinessApiResponse handlerSuccess(Object data, long start) {
        long took = System.currentTimeMillis() - start;
        return new BusinessApiResponse(AppCoreConstant.SUCCESS_CODE, AppCoreConstant.SUCCESS, StringUtils.EMPTY, took, data);
    }

    public BusinessApiResponse handlerSuccessAdmin(Object data) {
        return new BusinessApiResponse(AppCoreConstant.SUCCESS_CODE, AppCoreConstant.SUCCESS, data,null);
    }
    
    public BusinessApiResponse handlerSuccess(Object data, Integer statusCode ,long start) {
        long took = System.currentTimeMillis() - start;
        return new BusinessApiResponse(statusCode, AppCoreConstant.SUCCESS, StringUtils.EMPTY, took, data);
    }
}
