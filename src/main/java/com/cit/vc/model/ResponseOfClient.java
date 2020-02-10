package com.cit.vc.model;

public class ResponseOfClient {

	private int statusCodeValue;

	private String errorCode;

	private String descriptionMessage;

	private String response;

	private String contentTypeResponse;

	public int getStatusCodeValue() {
		return statusCodeValue;
	}

	public void setStatusCodeValue(int statusCodeValue) {
		this.statusCodeValue = statusCodeValue;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getDescriptionMessage() {
		return descriptionMessage;
	}

	public void setDescriptionMessage(String descriptionMessage) {
		this.descriptionMessage = descriptionMessage;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getContentTypeResponse() {
		return contentTypeResponse;
	}

	public void setContentTypeResponse(String contentTypeResponse) {
		this.contentTypeResponse = contentTypeResponse;
	}

}
