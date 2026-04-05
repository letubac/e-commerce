package com.ecommerce.webapp;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
/**
 * author: LeTuBac
 */
public class MovieBaseResponse {
	private int resultCode;
	private String resultDescription;
	private BusinessApiError error;
	private Object data;
	
	public MovieBaseResponse(int resultCode, String resultDescription, BusinessApiError error) {
		this.resultCode = resultCode;
		this.resultDescription = resultDescription;
		this.error = error;
	}
	public MovieBaseResponse(int resultCode, String resultDescription) {
		this.resultCode = resultCode;
		this.resultDescription = resultDescription;
	}

	public MovieBaseResponse(int resultCode, String resultDescription,Object data) {
		this.resultCode = resultCode;
		this.resultDescription = resultDescription;
		this.data = data;
	}
}
