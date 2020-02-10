package com.cit.vc.model;

import javax.jms.JMSException;
import javax.servlet.http.HttpServletRequest;

import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.cit.vc.utils.Headers;


public class MessageHeader {

	private String logId;

	private String stackId;

	//private final static String REQ_POSTFIX = "-req";
	//private final static String RES_POSTFIX = "-res";
	//private final static String DASH = "-";

	private String correlationId;
	//private String channel;
	//private String securityType;
	//private String instanceId;
	//private String messageType;
	//private String spanId;
	//private String traceId;
	//private String stan;
	private String serviceId;
	//private String walletShortCode;
	//private int stepOrder;

	//private String requestServiceId;
	//private String responseServiceId;
	//private String responseStatus;

	public MessageHeader() {
	};

	public MessageHeader(HttpServletRequest request) {
		setCorrelationId(request.getHeader(Headers.CORRELATION_ID.name()));
		setStackId(request.getHeader(Headers.stackId.name()));
		/*setChannel(request.getHeader(Headers.CHANNEL.name()));
		setSecurityType(request.getHeader(Headers.SECURITY_TYPE.name()));
		setInstanceId(request.getHeader(Headers.INSTANCE_ID.name()));
		setMessageType(request.getHeader(Headers.MESSAGE_TYPE.name()));
		setSpanId(request.getHeader(Headers.SPAN_ID.name()));
		setTraceId(request.getHeader(Headers.TRACE_ID.name()));
		setStan(request.getHeader(Headers.STAN.name()));*/
		setServiceId(request.getHeader(Headers.SERVICE_ID.name()));
		setLogId(request.getHeader(Headers.logId.name()));
		//setWalletShortCode(request.getHeader(Headers.WALLET_SHORT_CODE.name()));
		//setStepOrder(/*Integer.parseInt(request.getHeader(Headers.STEP_ORDER.name()))*/1);
		//setResponseStatus(request.getHeader(Headers.RESPONSE_STATUS.name()));

		//generateServiceIdForRequest();
		//generateServiceIdForResponse();

	}

	public MessageHeader(ActiveMQTextMessage msg) throws JMSException {

		setCorrelationId(msg.getCorrelationId());
		setStackId(msg.getStringProperty(Headers.stackId.name()));
		
		setServiceId(msg.getStringProperty(Headers.SERVICE_ID.name()));
		setLogId(msg.getStringProperty(Headers.logId.name()));

	}

	/*public String getResponseStatus() {
		return responseStatus;
	}

	public void setResponseStatus(String responseStatus) {
		this.responseStatus = responseStatus;
	}

	public String getChannel() {
		return channel;
	}

	private void setChannel(String channel) {
		this.channel = channel;
	}

	public String getSecurityType() {
		return securityType;
	}

	private void setSecurityType(String securityType) {
		this.securityType = securityType;
	}

	public String getInstanceId() {
		return instanceId;
	}

	private void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getMessageType() {
		return messageType;
	}

	private void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public String getSpanId() {
		return spanId;
	}

	private void setSpanId(String spanId) {
		this.spanId = spanId;
	}

	public String getTraceId() {
		return traceId;
	}

	private void setTraceId(String traceId) {
		this.traceId = traceId;
	}

	public String getStan() {
		return stan;
	}

	private void setStan(String stan) {
		this.stan = stan;
	}
*/
	public String getLogId() {
		return logId;
	}

	public void setLogId(String logId) {
		this.logId = logId;
	}

	public String getStackId() {
		return stackId;
	}

	public void setStackId(String stackId) {
		this.stackId = stackId;
	}
	
	public String getServiceId() {
		return serviceId;
	}

	private void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	/*public String getWalletShortCode() {
		return walletShortCode;
	}

	private void setWalletShortCode(String walletShortCode) {
		this.walletShortCode = walletShortCode;
	}

	*//**
	 * @return the stepOrder
	 *//*
	public int getStepOrder() {
		return stepOrder;
	}

	*//**
	 * @param stepOrder
	 *            the stepOrder to set
	 *//*
	private void setStepOrder(int stepOrder) {
		this.stepOrder = stepOrder;
	}
*/
	/**
	 * @return the correlationId
	 */
	public String getCorrelationId() {
		return correlationId;
	}

	/**
	 * @param correlationId
	 *            the correlationId to set
	 */
	private void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}

	/*private String generateServiceIdForRequest() {

		return requestServiceId = generateCommonServiceId(REQ_POSTFIX);
	}

	private String generateServiceIdForResponse() {

		return responseServiceId = generateCommonServiceId(RES_POSTFIX);

	}

	private String generateCommonServiceId(String postfix) {
		return new StringBuilder(serviceId).append(DASH).append(walletShortCode).append(DASH).append(stepOrder)
				.append(postfix).toString();
	}

	*//**
	 * @return the responseServiceId
	 *//*
	public String getResponseServiceId() {
		return responseServiceId;
	}

	public void setResponseServiceId(String responseServiceId) {
		this.responseServiceId = responseServiceId;
	}

	*//**
	 * @return the requestServiceId
	 *//*
	public String getRequestServiceId() {
		return requestServiceId;
	}

	public void setRequestServiceId(String requestServiceId) {
		this.requestServiceId = requestServiceId;
	}*/

	/*public HttpHeaders getHttpHeaders() {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add(Headers.CORRELATION_ID.name(), getCorrelationId());
		httpHeaders.add(Headers.CHANNEL.name(), getChannel());
		httpHeaders.add(Headers.INSTANCE_ID.name(), getInstanceId());
		httpHeaders.add(Headers.MESSAGE_TYPE.name(), getMessageType());
		httpHeaders.add(Headers.SECURITY_TYPE.name(), getSecurityType());
		httpHeaders.add(Headers.SPAN_ID.name(), getSpanId());
		httpHeaders.add(Headers.TRACE_ID.name(), getTraceId());
		httpHeaders.add(Headers.STAN.name(), getStan());
		httpHeaders.add(Headers.SERVICE_ID.name(), getServiceId());
		//httpHeaders.add(Headers.WALLET_SHORT_CODE.name(), getWalletShortCode());
		//httpHeaders.add(Headers.STEP_ORDER.name(), String.valueOf(getStepOrder()));
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		return httpHeaders;
	}*/

}
