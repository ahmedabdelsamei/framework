package com.cit.vc.model;

import java.util.HashMap;
import java.util.Map;

public class MappingRequest {

	private String url;

	private String request;

	private Map<String, String> headers = new HashMap<String, String>();

	private String methodType;

	private boolean enableSSL=false;
	private String certificatePath=null;
	private String keypassword=null;
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getRequest() {
		return request;
	}

	public void setRequest(String request) {
		this.request = request;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public Map<String, String> getHeaders() {
		return headers;
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

	public String getCertificatePath() {
		return certificatePath;
	}

	public String getKeypassword() {
		return keypassword;
	}

	public void setEnableSSL(boolean enableSSL) {
		this.enableSSL = enableSSL;
	}

	public void setCertificatePath(String certificatePath) {
		this.certificatePath = certificatePath;
	}

	public void setKeypassword(String keypassword) {
		this.keypassword = keypassword;
	}

	@Override
	public String toString() {
		return "[url=" + url + ", request=" + request + ", headers=" + headers + "]";
	}

}
