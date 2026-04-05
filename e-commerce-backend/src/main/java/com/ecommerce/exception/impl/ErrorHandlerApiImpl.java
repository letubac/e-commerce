package com.ecommerce.exception.impl;

import java.util.Locale;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import com.ecommerce.constant.AppCoreConstant;
import com.ecommerce.constant.BusinessExceptionCodeConstant;
import com.ecommerce.exception.DetailException;
import com.ecommerce.exception.ErrorHandler;
import com.ecommerce.exception.ExceptionCode;
import com.ecommerce.exception.GlobalException;
import com.ecommerce.exception.MessageError;
import com.ecommerce.util.MovieCollectionUtil;
import com.ecommerce.webapp.BusinessApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * ErrorHandlerApiImpl
 * 
 * @version 01-00
 * @since 01-00
 * @author BacLV
 */
@Component
@Slf4j
/**
 * author: LeTuBac
 */
public class ErrorHandlerApiImpl implements ErrorHandler {

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private HttpServletRequest request;

    public BusinessApiResponse handlerException(Exception ex, long start) {
        // Default message
        long took = System.currentTimeMillis() - start;
        ExceptionCode expCode = new ExceptionCode(BusinessExceptionCodeConstant.E500_ERROR_INTERNAL);
        boolean isTranslate = false;
        String message = ex.getMessage();
        String hiddenDesc = StringUtils.EMPTY;
        Object[] paramater = null;

        if (ex instanceof NullPointerException) {
            hiddenDesc = "NPE";
        } else if (ex instanceof GlobalException) {
            GlobalException globalException = (GlobalException) ex;
            expCode = globalException.getExceptionCode();
            if (ex instanceof DetailException) {
                DetailException detailException = (DetailException) globalException;
                isTranslate = detailException.isTranslate();
                message = detailException.getSpecificMsg();
                hiddenDesc = detailException.getSpecificMsg();
                paramater = detailException.getParamater();
            }
        }

        String code = expCode.getText();
        // DetailException stores the full error key (e.g. "905_REVIEW_ALREADY_EXISTS").
        // expCode.getText() strips the numeric prefix and returns only
        // "REVIEW_ALREADY_EXISTS",
        // which does not match any properties key. Use the full code for i18n lookup.
        if (ex instanceof DetailException) {
            String fullCode = ((DetailException) ex).getExceptionErrorCode();
            if (fullCode != null) {
                code = fullCode;
            }
        }

        if (isTranslate) {
            String ehLocaleHeader = Optional.ofNullable(this.request.getHeader("Accept-Language")).orElse("vi");
            String ehLang = ehLocaleHeader.split("[-,;]")[0].trim();
            Locale locale = new Locale(ehLang.isEmpty() ? "vi" : ehLang);
            try {
                String translated = this.messageSource.getMessage(code, paramater, locale);
                // setUseCodeAsDefaultMessage(true) returns the code itself when key not found
                // only accept if it's actually different from the key (i.e. was translated)
                if (translated != null && !translated.equals(code)) {
                    message = translated;
                } else {
                    // Try with default locale as fallback
                    translated = this.messageSource.getMessage(code, paramater, Locale.US);
                    if (translated != null && !translated.equals(code)) {
                        message = translated;
                    }
                }
            } catch (Exception msgEx) {
                log.warn("i18n lookup failed for code [{}]: {}", code, msgEx.getMessage());
            }
        }

        if (StringUtils.isBlank(message)) {
            message = code;
        }
        return new BusinessApiResponse(expCode.getValue(), AppCoreConstant.ERROR, message, hiddenDesc, took);
    }

    public BusinessApiResponse handlerException(int codeStatus, String message) {
        // Default message

        boolean isTranslate = false;

        if (isTranslate) {
            String ehLocaleHeader2 = Optional.ofNullable(this.request.getHeader("Accept-Language")).orElse("vi");
            String ehLang2 = ehLocaleHeader2.split("[-,;]")[0].trim();
            message = this.messageSource.getMessage(MessageError.ERROR_COMMON, null,
                    new Locale(ehLang2.isEmpty() ? "vi" : ehLang2));
        }

        return new BusinessApiResponse(codeStatus, message, null, null);
    }

    @Override
    public BusinessApiResponse handlerBindingResult(BindingResult bindingResult, long start) {
        // Default message
        String code = BusinessExceptionCodeConstant.E500_ERROR_INTERNAL;
        int codeStatus = 500;
        String message = null;
        Object data = null;
        if (null != bindingResult && MovieCollectionUtil.isNotEmpty(bindingResult.getAllErrors())) {
            String defaultMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();
            code = bindingResult.getAllErrors().get(0).getCode();
            Object[] args = bindingResult.getAllErrors().get(0).getArguments();
            data = bindingResult.getAllErrors();

            if (StringUtils.isBlank(code)) {
                code = BusinessExceptionCodeConstant.E500_ERROR_INTERNAL;
            }

            ExceptionCode expCode = new ExceptionCode(code);
            codeStatus = expCode.getValue();
            message = this.messageSource.getMessage(expCode.getText(), args,
                    new Locale(Optional.ofNullable(this.request.getHeader("Accept-Language")).orElse("en")));

            if (StringUtils.isNotBlank(defaultMessage)) {
                message = message.concat(System.lineSeparator()).concat(defaultMessage);
            }
        }

        return new BusinessApiResponse(codeStatus, AppCoreConstant.ERROR, null, message, data,
                AppCoreConstant.RESULT_CODE_SYSTEM_ERROR);

    }
}
