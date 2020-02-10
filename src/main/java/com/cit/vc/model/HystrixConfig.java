package com.cit.vc.model;

public class HystrixConfig {

	private int executionTimeout;

	private boolean fallbackEnabled;

	private boolean circuitBreakerEnabled;

	private boolean executionTimeoutEnabled;

	private int sleepWindow;

	private int errorThresholdPercentage;

	private int requestVolumeThreshold;

	private int metricsRollingWindow;

	private boolean allowDivergeFromCoreSize;

	private int coreSize;

	private int maximumSize;

	private int maxQueueSize;

	private int queueSizeRejectionThreshold;

	public int getExecutionTimeout() {
		return executionTimeout;
	}

	public void setExecutionTimeout(int executionTimeout) {
		this.executionTimeout = executionTimeout;
	}

	public boolean isFallbackEnabled() {
		return fallbackEnabled;
	}

	public void setFallbackEnabled(boolean fallbackEnabled) {
		this.fallbackEnabled = fallbackEnabled;
	}

	public boolean isCircuitBreakerEnabled() {
		return circuitBreakerEnabled;
	}

	public void setCircuitBreakerEnabled(boolean circuitBreakerEnabled) {
		this.circuitBreakerEnabled = circuitBreakerEnabled;
	}

	public boolean isExecutionTimeoutEnabled() {
		return executionTimeoutEnabled;
	}

	public void setExecutionTimeoutEnabled(boolean executionTimeoutEnabled) {
		this.executionTimeoutEnabled = executionTimeoutEnabled;
	}

	public int getSleepWindow() {
		return sleepWindow;
	}

	public void setSleepWindow(int sleepWindow) {
		this.sleepWindow = sleepWindow;
	}

	public int getErrorThresholdPercentage() {
		return errorThresholdPercentage;
	}

	public void setErrorThresholdPercentage(int errorThresholdPercentage) {
		this.errorThresholdPercentage = errorThresholdPercentage;
	}

	public int getRequestVolumeThreshold() {
		return requestVolumeThreshold;
	}

	public void setRequestVolumeThreshold(int requestVolumeThreshold) {
		this.requestVolumeThreshold = requestVolumeThreshold;
	}

	public int getMetricsRollingWindow() {
		return metricsRollingWindow;
	}

	public void setMetricsRollingWindow(int metricsRollingWindow) {
		this.metricsRollingWindow = metricsRollingWindow;
	}

	public boolean isAllowDivergeFromCoreSize() {
		return allowDivergeFromCoreSize;
	}

	public void setAllowDivergeFromCoreSize(boolean allowDivergeFromCoreSize) {
		this.allowDivergeFromCoreSize = allowDivergeFromCoreSize;
	}

	public int getCoreSize() {
		return coreSize;
	}

	public void setCoreSize(int coreSize) {
		this.coreSize = coreSize;
	}

	public int getMaximumSize() {
		return maximumSize;
	}

	public void setMaximumSize(int maximumSize) {
		this.maximumSize = maximumSize;
	}

	public int getMaxQueueSize() {
		return maxQueueSize;
	}

	public void setMaxQueueSize(int maxQueueSize) {
		this.maxQueueSize = maxQueueSize;
	}

	public int getQueueSizeRejectionThreshold() {
		return queueSizeRejectionThreshold;
	}

	public void setQueueSizeRejectionThreshold(int queueSizeRejectionThreshold) {
		this.queueSizeRejectionThreshold = queueSizeRejectionThreshold;
	}

}
