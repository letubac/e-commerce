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
        if (indexUnderlinedFirst < 0) {
            // No underscore: handle formats like "E955" or "951"
            String numericOnly = exceptionErrorCode.replaceAll("[^0-9]", "");
            this.value = StringUtils.isNotBlank(numericOnly) ? Integer.parseInt(numericOnly) : 500;
            this.text = exceptionErrorCode;
        } else {
            String valuePart = StringUtils.substring(exceptionErrorCode, 0, indexUnderlinedFirst);
            String numericOnly = valuePart.replaceAll("[^0-9]", "");
            this.value = StringUtils.isNotBlank(numericOnly) ? Integer.parseInt(numericOnly) : 500;
            this.text = StringUtils.substring(exceptionErrorCode, indexUnderlinedFirst + 1,
                    StringUtils.length(exceptionErrorCode));
        }
    }
}
