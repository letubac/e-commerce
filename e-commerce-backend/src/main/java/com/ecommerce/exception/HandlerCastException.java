package com.ecommerce.exception;


public interface HandlerCastException {
	 public void castException(Exception ex, String exceptionConstant)throws DetailException;

	    /**
	     * castException
	     * @param ex
	     * @param exceptionConstant
	     * @param param
	     * @throws DetailException
	     * @author BacLV
	     */
	    void castException(Exception ex, String exceptionConstant, String[] param) throws DetailException;
}
