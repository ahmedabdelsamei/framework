package com.cit.vc.model.schema;

public class TransactionStatusSchema {
	String targetSystem;
	String STAN;
	Body body;

	public String getTargetSystem() {
		return targetSystem;
	}

	public void setTargetSystem(String targetSystem) {
		this.targetSystem = targetSystem;
	}

	public String getSTAN() {
		return STAN;
	}

	public void setSTAN(String sTAN) {
		STAN = sTAN;
	}

	public Body getBody() {
		return body;
	}

	public void setBody(Body body) {
		this.body = body;
	}

	@Override
	public String toString() {
		return "TransactionStatusSchema [targetSystem=" + targetSystem + ", STAN=" + STAN + ", body=" + body + "]";
	}

}

