package com.ecommerce.constant;

public class BusinessExceptionCodeConstant {


	// ERROR AUTHEN
	/** Unauthorized */ 
	public static final String E401_UNAUTHORIZED = "401_UNAUTHORIZED";
	/** Forbidden */ 
	public static final String E401_FORBIDDEN = "401_FORBIDDEN";

	// ERROR DATABASE (600 - 699)
	/** Error connect database */ 
	public static final String E601_ERROR_DB = "601_ERROR_DB";
	/** Error data not found */ 
	public static final String E602_ERROR_DATA_NOT_FOUND = "602_ERROR_DATA_NOT_FOUND";

	// ERROR HYPERTEXT TRANSFER PROTOCOL (000 - 599)
	/** ERROR_INTERNAL */ 
	public static final String E500_ERROR_INTERNAL = "500_ERROR_INTERNAL";
	/** Error timeout request */ 
	public static final String E501_TIME_OUT = "501_TIME_OUT";
	/** Error invalid params */ 
	public static final String E502_INVALID_INPUT = "502_INVALID_INPUT";
	/** Error invalid header */ 
	public static final String E503_INVALID_HEADER = "503_INVALID_HEADER";
}
