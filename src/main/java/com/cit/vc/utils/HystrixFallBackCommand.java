package com.cit.vc.utils;

import java.util.function.Function;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cit.vc.model.HystrixPojo;
import com.cit.vc.model.ResponseOfClient;
import com.google.gson.Gson;
import com.netflix.config.ConfigurationManager;
import com.netflix.hystrix.Hystrix;
import com.netflix.hystrix.HystrixCircuitBreaker;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;

public class HystrixFallBackCommand extends HystrixCommand<String> {

	private static final Logger logger = LoggerFactory.getLogger("filelogger");

	private Function<HystrixPojo, String> function;
	private HystrixPojo hystrixPojo;

	public HystrixFallBackCommand(Function<HystrixPojo, String> function, HystrixPojo hystrixPojo) {

		super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(hystrixPojo.getMessageHeader().getServiceId()))
				.andCommandKey(HystrixCommandKey.Factory.asKey(hystrixPojo.getMessageHeader().getServiceId()))
				//.andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(hystrixPojo.getMessageHeader().getServiceId()))
				.andCommandPropertiesDefaults(

						HystrixCommandProperties.Setter()
								.withFallbackIsolationSemaphoreMaxConcurrentRequests(Integer.MAX_VALUE)
								.withExecutionTimeoutInMilliseconds(
										hystrixPojo.getHystrixConfig().getExecutionTimeout())
								.withFallbackEnabled(hystrixPojo.getHystrixConfig().isFallbackEnabled())
								.withCircuitBreakerEnabled(hystrixPojo.getHystrixConfig().isCircuitBreakerEnabled())
								.withExecutionTimeoutEnabled(hystrixPojo.getHystrixConfig().isExecutionTimeoutEnabled())
								.withCircuitBreakerSleepWindowInMilliseconds(
										hystrixPojo.getHystrixConfig().getSleepWindow())
								.withCircuitBreakerErrorThresholdPercentage(
										hystrixPojo.getHystrixConfig().getErrorThresholdPercentage())
								.withCircuitBreakerRequestVolumeThreshold(
										hystrixPojo.getHystrixConfig().getRequestVolumeThreshold())
								.withMetricsRollingStatisticalWindowInMilliseconds(
										hystrixPojo.getHystrixConfig().getMetricsRollingWindow())

				)
				.andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter()
						.withAllowMaximumSizeToDivergeFromCoreSize(
								hystrixPojo.getHystrixConfig().isAllowDivergeFromCoreSize())
						.withCoreSize(hystrixPojo.getHystrixConfig().getCoreSize())
						.withMaximumSize(hystrixPojo.getHystrixConfig().getMaximumSize())
						.withMaxQueueSize(hystrixPojo.getHystrixConfig().getMaxQueueSize())
						.withQueueSizeRejectionThreshold(
								hystrixPojo.getHystrixConfig().getQueueSizeRejectionThreshold())));

		// if there is a change , will change in cached Command Properties
		String prefixOfCommandProperty = "hystrix.command." + hystrixPojo.getMessageHeader().getServiceId() + ".";
		AbstractConfiguration configuration = ConfigurationManager.getConfigInstance();
		configuration.setProperty(prefixOfCommandProperty + "execution.isolation.thread.timeoutInMilliseconds",
				hystrixPojo.getHystrixConfig().getExecutionTimeout());
		configuration.setProperty(prefixOfCommandProperty + "fallback.enabled",
				hystrixPojo.getHystrixConfig().isFallbackEnabled());
		configuration.setProperty(prefixOfCommandProperty + "circuitBreaker.enabled",
				hystrixPojo.getHystrixConfig().isCircuitBreakerEnabled());
		configuration.setProperty(prefixOfCommandProperty + "execution.timeout.enabled",
				hystrixPojo.getHystrixConfig().isExecutionTimeoutEnabled());
		configuration.setProperty(prefixOfCommandProperty + "circuitBreaker.sleepWindowInMilliseconds",
				hystrixPojo.getHystrixConfig().getSleepWindow());
		configuration.setProperty(prefixOfCommandProperty + "circuitBreaker.errorThresholdPercentage",
				hystrixPojo.getHystrixConfig().getErrorThresholdPercentage());
		configuration.setProperty(prefixOfCommandProperty + "circuitBreaker.requestVolumeThreshold",
				hystrixPojo.getHystrixConfig().getRequestVolumeThreshold());
		configuration.setProperty(prefixOfCommandProperty + "metrics.rollingStats.timeInMilliseconds",
				hystrixPojo.getHystrixConfig().getMetricsRollingWindow());

		// if there is a change , will change in cached Thread Pool Properties
		/*String prefixOfThreadPoolProperty = "hystrix.threadpool." + hystrixPojo.getMessageHeader().getServiceId() + ".";
		configuration.setProperty(prefixOfThreadPoolProperty + "allowMaximumSizeToDivergeFromCoreSize",
				hystrixPojo.getHystrixConfig().isAllowDivergeFromCoreSize());
		configuration.setProperty(prefixOfThreadPoolProperty + "coreSize",
				hystrixPojo.getHystrixConfig().getCoreSize());
		configuration.setProperty(prefixOfThreadPoolProperty + "maximumSize",
				hystrixPojo.getHystrixConfig().getMaximumSize());
		// configuration.setProperty(prefixOfThreadPoolProperty + "maxQueueSize",
		// hystrixPojo.getHystrixConfig().getMaxQueueSize());
		configuration.setProperty(prefixOfThreadPoolProperty + "queueSizeRejectionThreshold",
				hystrixPojo.getHystrixConfig().getQueueSizeRejectionThreshold());*/

		this.function = function;
		this.hystrixPojo = hystrixPojo;

	}

	public HystrixFallBackCommand(Function<HystrixPojo, String> function, HystrixPojo hystrixPojo,
			String defaultConfig) {

		super(HystrixCommand.Setter
				.withGroupKey(HystrixCommandGroupKey.Factory.asKey(hystrixPojo.getMessageHeader().getServiceId()))
				.andCommandKey(HystrixCommandKey.Factory.asKey(hystrixPojo.getMessageHeader().getServiceId())));

		this.function = function;
		this.hystrixPojo = hystrixPojo;

	}

	@Override
	protected String run() /* throws Exception */ {
		return function.apply(hystrixPojo);
	}

	@Override
	protected String getFallback() {

		String response;
		ResponseOfClient responseOfClient = new ResponseOfClient();



		if (StringUtils.isBlank(hystrixPojo.getResponseInCaseOfException())) {
			if (HystrixCircuitBreaker.Factory
					.getInstance(HystrixCommandKey.Factory.asKey(hystrixPojo.getMessageHeader().getServiceId()))
					.isOpen() && hystrixPojo.isInternalCall()) {

				responseOfClient.setStatusCodeValue(1);
				responseOfClient.setErrorCode("IFW03");
				responseOfClient.setDescriptionMessage("hystrix reverse");
				responseOfClient.setContentTypeResponse("json");
				String hystrixResponse = "{\"errorCode\":\"IFW03\", \"description\": \"hystrix reverse\"}";
				responseOfClient.setResponse(hystrixResponse);

			} else {

				logger.info("hystrix fall back, not reverse");


				responseOfClient.setStatusCodeValue(2);
				responseOfClient.setErrorCode("IFW04");
				responseOfClient.setDescriptionMessage("hystrix not reverse");
				responseOfClient.setContentTypeResponse("json");
				String hystrixResponse = "{\"errorCode\":\"IFW04\", \"description\":\"" +  getExecutionEvents() + "\"}";
				responseOfClient.setResponse(hystrixResponse);
				//responseOfClient.setResponse("hystrix not reverse");
			}
			response = new Gson().toJson(responseOfClient);
		} else {
			response = hystrixPojo.getResponseInCaseOfException();
		}

		return response;
	}

}
