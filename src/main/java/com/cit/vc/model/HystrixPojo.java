package com.cit.vc.model;

import java.util.HashMap;
import java.util.Map;

public class HystrixPojo {

	private String url;

	private MessageHeader messageHeader;

	private String request;

	private HystrixConfig hystrixConfig;

	private boolean internalCall;

	private String responseInCaseOfException = "";

	private Map<String, String> headers = new HashMap<String, String>();

	private String methodType;
	private boolean enableSSL;
	private String certificateParh;
	private String keyPassword;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public MessageHeader getMessageHeader() {
		return messageHeader;
	}

	public void setMessageHeader(MessageHeader messageHeader) {
		this.messageHeader = messageHeader;
	}

	public String getRequest() {
		return request;
	}

	public void setRequest(String request) {
		this.request = request;
	}

	public HystrixConfig getHystrixConfig() {
		return hystrixConfig;
	}

	public void setHystrixConfig(HystrixConfig hystrixConfig) {
		this.hystrixConfig = hystrixConfig;
	}

	public boolean isInternalCall() {
		return internalCall;
	}

	public void setInternalCall(boolean internalCall) {
		this.internalCall = internalCall;
	}

	public String getResponseInCaseOfException() {
		return responseInCaseOfException;
	}

	public void setResponseInCaseOfException(String responseInCaseOfException) {
		this.responseInCaseOfException = responseInCaseOfException;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public String getMethodType() {
		return methodType;
	}

	public void setMethodType(String methodType) {
		this.methodType = methodType;
	}

	public boolean isEnableSSL() {
		return enableSSL;
	}

	public String getCertificateParh() {
		return certificateParh;
	}

	public String getKeyPassword() {
		return keyPassword;
	}

	public void setEnableSSL(boolean enableSSL) {
		this.enableSSL = enableSSL;
	}

	public void setCertificateParh(String certificateParh) {
		this.certificateParh = certificateParh;
	}

	public void setKeyPassword(String keyPassword) {
		this.keyPassword = keyPassword;
	}

}
