package com.cit.vc.model;

public class MappingResponse {

	private String response;

	private int statusCodeValue;

	private String bussinessMessage;

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public int getStatusCodeValue() {
		return statusCodeValue;
	}

	public void setStatusCodeValue(int statusCodeValue) {
		this.statusCodeValue = statusCodeValue;
	}

	public String getBussinessMessage() {
		return bussinessMessage;
	}

	public void setBussinessMessage(String bussinessMessage) {
		this.bussinessMessage = bussinessMessage;
	}

}
