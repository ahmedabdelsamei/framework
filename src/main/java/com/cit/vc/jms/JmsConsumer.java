package com.cit.vc.jms;

import java.util.Date;

import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.cit.vc.exceptions.IntegrationFrameworkGeneralException;
import com.cit.vc.model.LogModel;
import com.cit.vc.model.MappingRequest;
import com.cit.vc.model.MessageHeader;
import com.cit.vc.model.ResponseOfClient;
import com.cit.vc.service.DynamicClientService;
import com.cit.vc.service.DynamicMappingService;
import com.google.gson.Gson;

import net.minidev.json.JSONValue;

@Component
public class JmsConsumer {
	
	@Autowired
	private JmsPublisher jmsPublisher;
	
	@Value("${queues.input-queue}")
	private String requestQueue;
	
	@Value("${queues.output-queue}")
	private String responseQueue;
	
	@Autowired
	private DynamicMappingService dynamicMappingService;
	
	@Autowired
	private DynamicClientService dynamicClientService;

	private static final Logger logger = LoggerFactory.getLogger("filelogger");

	@JmsListener(destination = "vc-integration-bm-req", containerFactory = "defaultContainerFactory")
	public void receive(Object msg) {

		try {


			ActiveMQTextMessage activeMQTextMessage = (ActiveMQTextMessage) msg;

			String busMsg = activeMQTextMessage.getText();

			MessageHeader messageHeader = new MessageHeader(activeMQTextMessage);
			logger.info(messageHeader.getServiceId()+":- send reponse of message correlation ID "+messageHeader.getCorrelationId()+" to queue response...");
			if (StringUtils.isAllBlank(messageHeader.getServiceId(), busMsg)){
				throw new IntegrationFrameworkGeneralException("serviceId or busMsg is null or empty");
			}
			
			long startTime1 = System.currentTimeMillis();

			MappingRequest mappingRequest = dynamicMappingService.getRequest(busMsg, messageHeader.getServiceId(),messageHeader.getCorrelationId());

			Date requestDate = new Date();

			long endTime1 = System.currentTimeMillis();

			 //System.err.println("time 1: " + (endTime1 - startTime1));

			long startTime2 = System.currentTimeMillis();
			
			ResponseOfClient responseOfClient = dynamicClientService.sendDynamicRequestAndRecieveResponse(
					mappingRequest, messageHeader);
			long endTime2 = System.currentTimeMillis();
			//System.err.println("time 2: " + (endTime2 - startTime2));
			
			Date responseDate = new Date();
			
			LogModel logModel = new LogModel();
			logModel.setLogId(messageHeader.getLogId());
			logModel.setStackId(messageHeader.getStackId());
			logModel.setRequest(mappingRequest.getRequest());
			logModel.setRequestDate(requestDate.toString());
			logModel.setResponse(responseOfClient.getResponse());
			logModel.setResponseDate(responseDate.toString());
			
			//JSONObject jsonObject = new JSONObject(logModel);
			
			jmsPublisher.send(/*StringEscapeUtils.unescapeJava(*/new Gson().toJson(logModel));
			
			
			

			long startTime3 = System.currentTimeMillis();
			
			String response = dynamicMappingService.getResponse(busMsg, responseOfClient,
					messageHeader.getServiceId());
			
			long endTime3 = System.currentTimeMillis();
			
			//System.err.println("time 3: " + (endTime3 - startTime3));
			//long startTime4 = System.currentTimeMillis();
			
			jmsPublisher.sendResponse(responseQueue, response, messageHeader);
			
			//long endTime4 = System.currentTimeMillis();
			
			//System.err.println("time 4: " + (endTime4 - startTime4));
	
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Error with Mapping jsonBussinessMessage: " + e.getMessage());
		}

	}

}