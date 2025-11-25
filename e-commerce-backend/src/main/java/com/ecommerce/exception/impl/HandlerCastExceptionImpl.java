package com.ecommerce.exception.impl;
import org.springframework.stereotype.Service;

import com.ecommerce.exception.DetailException;
import com.ecommerce.exception.HandlerCastException;

@Service("handlerCastException")
public class HandlerCastExceptionImpl implements HandlerCastException{


    @Override
    public void castException(Exception ex, String exceptionConstant)throws DetailException {
    	// ActivitiException removed because Activiti is not supported in Spring Boot 3
        if (ex instanceof DetailException) {
            DetailException detailException = (DetailException) ex;
            String exceptionErrorCode = detailException.getExceptionErrorCode();
            throw new DetailException(exceptionErrorCode,detailException.getParamater(),true);
        } else {
            throw new DetailException(exceptionConstant, true);
        }
    }
    
    @Override
    public void castException(Exception ex, String exceptionConstant,String[] param)throws DetailException {
        if (ex instanceof DetailException) {
            DetailException detailException = (DetailException) ex;
            String exceptionErrorCode = detailException.getExceptionErrorCode();
            throw new DetailException(exceptionErrorCode,detailException.getParamater(),true);
        } else {
            throw new DetailException(exceptionConstant, param, true);
        }
    }

}
