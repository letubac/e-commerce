package com.ecommerce.exception;


import org.apache.commons.lang3.StringUtils;

import com.ecommerce.constant.AppCoreConstant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExceptionCode {

    private String text;
    private int value;
 
    public ExceptionCode(String exceptionErrorCode) {
        int indexUnderlinedFirst = StringUtils.indexOf(exceptionErrorCode, AppCoreConstant.UNDERLINED);
        this.value = Integer.parseInt(StringUtils.substring(exceptionErrorCode, 0, indexUnderlinedFirst));
        this.text = StringUtils.substring(exceptionErrorCode, indexUnderlinedFirst + 1, StringUtils.length(exceptionErrorCode));
    }
}
