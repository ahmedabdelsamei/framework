package com.cit.vc.model.schema;

import java.util.List;

public class Body {

	List<TransactionDetail> transactionsDetails;

	public List<TransactionDetail> getTransactionsDetails() {
		return transactionsDetails;
	}

	public void setTransactionsDetails(List<TransactionDetail> transactionsDetails) {
		this.transactionsDetails = transactionsDetails;
	}

	@Override
	public String toString() {
		return "Body [transactionsDetails=" + transactionsDetails + "]";
	}

}
