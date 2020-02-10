package com.cit.vc.controller;

import java.net.SocketTimeoutException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.cit.vc.exceptions.IntegrationFrameworkGeneralException;
import com.cit.vc.jms.JmsPublisher;
import com.cit.vc.model.LogModel;
import com.cit.vc.model.MappingRequest;
import com.cit.vc.model.MessageHeader;
import com.cit.vc.model.ResponseOfClient;
import com.cit.vc.service.DynamicClientService;
import com.cit.vc.service.DynamicMappingService;
import com.cit.vc.service.ListenOnFilesProperties;
import com.cit.vc.service.XmlRequestCreation;
import com.cit.vc.utils.Common;
import com.google.gson.Gson;

@RestController
public class ExternalIntegration {

	@Value("${path.File}")
	private String path;

	@Autowired
	private XmlRequestCreation xmlRequestCreation;
	
	@Autowired
	private DynamicMappingService dynamicMappingService;

	@Autowired
	private DynamicClientService dynamicClientService;

	@Autowired
	private ListenOnFilesProperties listenOnFilesProperties;
	
	@Autowired
	private JmsPublisher jmsPublisher;

	private static final Logger logger = LoggerFactory.getLogger("filelogger");

	@RequestMapping(value = "/integrate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
	public ResponseEntity<?> integrate(@RequestBody String businessMessage, HttpServletRequest request) {
		long fullCycleStartTime = System.currentTimeMillis();
		logger.info("receive business message: "+ businessMessage);
		MessageHeader messageHeader = new MessageHeader(request);
		logger.info("start processing request with correlationId "+messageHeader.getCorrelationId()+" and serviceId "+ messageHeader.getServiceId());
		String response = returnResponse(messageHeader, businessMessage);
		long fullCycleEndTime = System.currentTimeMillis();
		//System.out.println("full Cycle duration of request : "+ messageHeader.getServiceId()+"is " + (fullCycleEndTime - fullCycleStartTime));
		return new ResponseEntity<String>(response, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/getAPIRequestStructure", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
	public ResponseEntity<?> getAPIRequest(HttpServletRequest request) {

		logger.info("calling get api request structure");

		String serviceId = request.getHeader("serviceId");

		String response = dynamicMappingService.getRequestStructure(serviceId);

		return new ResponseEntity<String>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/generateReq", produces = MediaType.APPLICATION_XML_VALUE, method = RequestMethod.GET)
	public ResponseEntity<?> generateXmlRequest(HttpServletRequest request) {

		logger.info("generate xml request");

		String wsdlUrl = request.getHeader("wsdlUrl");
		String serviceId = request.getHeader("serviceId");
		
		String clientName = Common.splitString(serviceId, "-")[0];
		String serviceName = Common.splitString(serviceId, "-")[1];

		String pathOfXmlFile = clientName + "-" + serviceName + ".xml";

//		XmlRequestCreation xmlRequestCreation = new XmlRequestCreation();
		String response = null;

		if (!(listenOnFilesProperties.getSaveAllFileProperties().containsKey(pathOfXmlFile))) {
			// long startTime = System.currentTimeMillis();
			response = xmlRequestCreation.createRequest(wsdlUrl, clientName, serviceName, path);
			// long endTime = System.currentTimeMillis();

			// System.err.println("Time is : " + (endTime - startTime));
		}else{
			response = "Request file is already exist";
		}

		return new ResponseEntity<String>(response, HttpStatus.OK);
	}


	private String returnResponse(MessageHeader messageHeader, String businessMessage) {
		
		if (StringUtils.isAllBlank(messageHeader.getServiceId(), businessMessage)){
			throw new IntegrationFrameworkGeneralException("serviceId or busMsg is not set ot empty");
		}
		
		MappingRequest mappingRequest = dynamicMappingService.getRequest(businessMessage,messageHeader.getServiceId(),messageHeader.getCorrelationId());
		Date requestDate = new Date();
		long hystricReponseStartTime = System.currentTimeMillis();
		ResponseOfClient responseOfClient = dynamicClientService.sendDynamicRequestAndRecieveResponse(
				mappingRequest, messageHeader);
		Date responseDate = new Date();
		 
		LogModel logModel = new LogModel();
		logModel.setLogId(messageHeader.getLogId());
		logModel.setStackId(messageHeader.getStackId());
		logModel.setRequest(mappingRequest.getRequest());
		logModel.setRequestDate(requestDate.toString());
		logModel.setResponse(responseOfClient.getResponse());
		logModel.setResponseDate(responseDate.toString());

		long hystricReponseEndTime = System.currentTimeMillis();
		//logger.info("hystricReponseTime of request "+ messageHeader.getServiceId()+"is " + (hystricReponseEndTime - hystricReponseStartTime));
		long prepareResponsestartTime = System.currentTimeMillis();
		String response = dynamicMappingService.getResponse(businessMessage, responseOfClient,messageHeader.getServiceId());
		long prepareResponseEndTime = System.currentTimeMillis();
		//logger.info("ResponseTime of request "+messageHeader.getServiceId()+" is " + (prepareResponseEndTime - prepareResponsestartTime));
		return response;
	}
	
	
	@ExceptionHandler(IntegrationFrameworkGeneralException.class)
	@ResponseBody
	public ResponseEntity<?> integrationFrameworkGeneralException(IntegrationFrameworkGeneralException e) {
		String response = "{\"errorCode\":\"IFW00\", \"description\":\"" + e.getMessage() + "\"}";
		return new ResponseEntity<String>(response, HttpStatus.OK);
	}

	@ResponseStatus(value = HttpStatus.GATEWAY_TIMEOUT, reason = "timeout request")
	@ExceptionHandler(SocketTimeoutException.class)
	@ResponseBody
	public ResponseEntity<?> socketTimeoutException(SocketTimeoutException e, HttpServletRequest request) {
		String response = "{\"errorCode\":\"IFW01\", \"description\":\"" + e.getMessage() + "\"}";
		return new ResponseEntity<String>(response, HttpStatus.OK);
	}
}
