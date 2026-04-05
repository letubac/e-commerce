package com.ecommerce.exception;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import com.ecommerce.constant.AppCoreConstant;
import com.ecommerce.constant.BusinessExceptionCodeConstant;
import com.ecommerce.webapp.BusinessApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private HttpServletRequest request;

	private void logMessage(String uri, String message) {
		log.error("Call API {} with error {} ", uri, message);
	}

	// Custom Error
	@ExceptionHandler(DetailException.class)
	public BusinessApiResponse detailException(DetailException ex, WebRequest request) {
		String errorCode = ex.getExceptionErrorCode();
		logMessage(((ServletWebRequest) request).getRequest().getRequestURI(),
				errorCode != null ? errorCode : "DetailException");
		String localeHeader = Optional.ofNullable(this.request.getHeader("Accept-Language")).orElse("vi");
		String lang = localeHeader.split("[-,;]")[0].trim();
		Locale locale = new Locale(lang.isEmpty() ? "vi" : lang);
		String message;
		if (errorCode != null && ex.isTranslate()) {
			try {
				message = messageSource.getMessage(errorCode, ex.getParamater(), locale);
			} catch (Exception e) {
				message = StringUtils.isNotBlank(ex.getSpecificMsg()) ? ex.getSpecificMsg() : errorCode;
			}
		} else {
			message = StringUtils.isNotBlank(ex.getSpecificMsg()) ? ex.getSpecificMsg()
					: (errorCode != null ? errorCode : "Lỗi hệ thống");
		}
		ExceptionCode expCode = ex.getExceptionCode();
		int statusCode = expCode != null ? expCode.getValue() : 400;
		return new BusinessApiResponse(statusCode, AppCoreConstant.ERROR, message, AppCoreConstant.EMPTY, 0);
	}

	@ExceptionHandler(DuplicateException.class)
	public BusinessApiResponse duplicateException(DuplicateException ex, WebRequest request) {
		logMessage(((ServletWebRequest) request).getRequest().getRequestURI(), ex.getMessage());
		ExceptionCode expCode = new ExceptionCode(BusinessExceptionCodeConstant.E500_ERROR_INTERNAL);
		String hiddenDesc = StringUtils.EMPTY;
		return new BusinessApiResponse(expCode.getValue(), AppCoreConstant.ERROR, ex.getMessage(), hiddenDesc, 0);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	BusinessApiResponse handleConstraintViolationException(ConstraintViolationException e) {

		int errorCode = 500;
		List<String> errorMessages = new ArrayList<>();
		for (ConstraintViolation<?> constraintViolation : e.getConstraintViolations()) {
			String messageTemplate = constraintViolation.getMessageTemplate();
			ExceptionCode expCode = new ExceptionCode(messageTemplate);
			errorCode = expCode.getValue();
			String messageCode = expCode.getText();
			PathImpl path = (PathImpl) constraintViolation.getPropertyPath();
			String[] params = new String[] { path.getLeafNode().asString() };
			String cvLocaleHeader = Optional.ofNullable(this.request.getHeader("Accept-Language")).orElse("en");
			String cvLang = cvLocaleHeader.split("[-,;]")[0].trim();
			Locale cvLocale = new Locale(cvLang.isEmpty() ? "en" : cvLang);
			String message = this.messageSource.getMessage(messageCode, params, cvLocale);
			errorMessages.add(message);
		}

		return new BusinessApiResponse(errorCode, AppCoreConstant.ERROR, String.join("; ", errorMessages),
				AppCoreConstant.EMPTY, 0);
	}
}
