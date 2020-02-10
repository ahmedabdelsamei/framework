package com.cit.vc.model.schema;

public class TransactionDetail {
	String externalTransactionId;
	int transactionId;
	int statusCode;
	String errorCode;

	public String getExternalTransactionId() {
		return externalTransactionId;
	}

	public void setExternalTransactionId(String externalTransactionId) {
		this.externalTransactionId = externalTransactionId;
	}

	public int getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(int transactionId) {
		this.transactionId = transactionId;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	@Override
	public String toString() {
		return "transactionsDetail [externalTransactionId=" + externalTransactionId + ", transactionId=" + transactionId
				+ ", statusCode=" + statusCode + ", errorCode=" + errorCode + "]";
	}

}