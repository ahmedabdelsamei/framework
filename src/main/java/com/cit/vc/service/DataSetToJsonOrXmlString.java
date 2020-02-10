package com.cit.vc.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.cit.vc.exceptions.IntegrationFrameworkGeneralException;
import com.cit.vc.model.ResponseOfClient;
import com.cit.vc.utils.Common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

@Component
public class DataSetToJsonOrXmlString {

	private static final Logger logger = LoggerFactory.getLogger("filelogger");

	private ObjectMapper objectMapper = new ObjectMapper();
	private static final String JSON_PATH_SYMBOL = "$.";
	private static final String DOT = ".";
	private static final String SLASH = "/";
	private static final String DOUBLE_SLASH = "//";
	private static final String COLON = ":";
	private static final String EXTRA_FIELDS = "extraFields";
	private static final String BODY = "//Body/";
	private static final String HEADER = "//Header/";
	private static final String PATH_OF_BODY = "/Envelope/Body";
	private static final String REGEX_LEFT_CURLY_BRACE = "\\{";
	private static final String REGEX_RIGHT_CURLY_BRACE = "\\}";
	private static final String DEFAULT_ERROR_CODE = "errorCode.default";
	private static final String ERROR_CODE = "errorCode.";
	private static final String ERROR_FLAG_BUS_MSG = "status.errorFlag";
	private static final String ERROR_CODE_BUS_MSG = "status.statusCode";
	private static final String XML_NAME = "xml";
	private static final String JSON = "json";
	private static final String QUESTION_MARK = "?";
	private static final String DOT_HEADER = ".Header.";

	private static final Configuration configuration = Configuration.builder()
			.jsonProvider(new JacksonJsonNodeJsonProvider()).mappingProvider(new JacksonMappingProvider()).build();

	@Autowired
	private DynamicMappingService dynamicMappingService;

	/*
	 * set data to elements of each service of soap request
	 */

	public void setElementsData(Document doc, String resultPath, String bussinessMessage, String clientName, String serviceName) {

		long startTime2 = System.currentTimeMillis();

		String removeBodyOrHeader = null;
		boolean checkForHeaders = false;
		if (resultPath.contains(HEADER)) {
			removeBodyOrHeader = StringUtils.replace(resultPath, HEADER, "");
			checkForHeaders = true;
		} else {
			removeBodyOrHeader = StringUtils.replace(resultPath, BODY, "");
		}
		Node node = null;
		StringBuilder stringBuilder = new StringBuilder();
		String afterReplaceSlashesWithDots = null;

		if (removeBodyOrHeader.contains(COLON)) {

			String[] splitWithSlash = Common.splitString(removeBodyOrHeader, SLASH);
			stringBuilder.append(SLASH);

			for (String split : splitWithSlash) {
				stringBuilder.append(SLASH + split.substring(split.indexOf(COLON) + 1));
			}

			try {
				node = (Node) Common.returnxPath(doc, stringBuilder.toString(), Node.class);
			} catch (IntegrationFrameworkGeneralException e) {
				throw e;
			}

			afterReplaceSlashesWithDots = Common.removeSlashes(stringBuilder.toString(), DOT/* QUESTION_MARK */);
		} else {
			node = (Node) Common.returnxPath(doc, DOUBLE_SLASH + removeBodyOrHeader, Node.class);
			afterReplaceSlashesWithDots = Common.removeSlashes(removeBodyOrHeader, DOT/* QUESTION_MARK */);
		}

		String replaceDotSlash = StringUtils.replace(afterReplaceSlashesWithDots, DOT/* QUESTION_MARK */, SLASH);

		String value = null;

		if (checkForHeaders) {
			value = (String) dynamicMappingService.getMapClientProperties().get(clientName)
					.get(serviceName + DOT_HEADER + afterReplaceSlashesWithDots);
			// start fix added by ibrahim Osama 1/5/2019
			if (value ==null || (value !=null && value.equals("")) ){
				throw new IntegrationFrameworkGeneralException("xpath "+afterReplaceSlashesWithDots+ " is not mapped correctly please add a valid mapping path");
			}
			// end fix added by ibrahim Osama 1/5/2019
			}

		else if (dynamicMappingService.getMapOfVericashAndClient().get(clientName)
				.containsKey(afterReplaceSlashesWithDots)) {
			String replaceDotWithSlash = SLASH + StringUtils.replace(dynamicMappingService.getMapOfVericashAndClient()
					.get(clientName).get(afterReplaceSlashesWithDots).toString(), DOT, SLASH);
			// String searchInCommonMapExpression = /*JSON_PATH_SYMBOL +*/
			// replaceDotWithSlash;
			value = getJsonPathDataFromJson(replaceDotWithSlash, bussinessMessage);
			// start fix added by ibrahim Osama 1/5/2019
			if (value ==null || (value !=null && value.equals("")) ){
				throw new IntegrationFrameworkGeneralException("xpath "+afterReplaceSlashesWithDots+ " is not mapped correctly please add a valid mapping path");
			}
			// end fix added by ibrahim Osama 1/5/2019
		} else {

			String extraFieldsExpression = /* JSON_PATH_SYMBOL */SLASH + EXTRA_FIELDS + /* DOT */SLASH;
			String searchInExtraFieldExpression = extraFieldsExpression + replaceDotSlash;
			value = getJsonPathDataFromJson(searchInExtraFieldExpression, bussinessMessage);
		}

		if(node != null)
			node.appendChild(doc.createTextNode(value));
		long endTime2 = System.currentTimeMillis();

		// System.err.println("time 1 : " + (endTime2 - startTime2));
	}


	/*
	 * get data from JSON path Data
	 */

	private String getJsonPathDataFromJson2(String expression, String jsonData) {

		// logger.info("get json path data");

		long startTime2 = System.currentTimeMillis();
		JsonNode jsonNode = null;
		try {
			jsonNode = objectMapper.readValue(jsonData, JsonNode.class);
			// jsonNode = objectMapper.readTree(jsonData);
			long endTime2 = System.currentTimeMillis();

			// System.err.println("time 2 : " + (endTime2 - startTime2));
		} catch (JsonProcessingException e) {
			logger.error("error during parsing string to json using jsonNode and object with erro message [{}]",
					e.getMessage());
			throw new IntegrationFrameworkGeneralException(
					"error during parsing string to json using jsonNode and object with erro message [" + e.getMessage()
							+ "]");
		} catch (IOException e) {
			logger.error("error during parsing string to json using jsonNode and object with erro message [{}]",
					e.getMessage());
			throw new IntegrationFrameworkGeneralException(
					"error during parsing string to json using jsonNode and object with erro message [" + e.getMessage()
							+ "]");
		}

		Object result = null;
		try {
			result = (Object) jsonNode.at(expression);

		} catch (Exception e) {
			return "";
		}

		return result.toString();
	}
	/*
	 * get data from JSON path Data
	 */

	private String getJsonPathDataFromJson(String expression, String jsonData) {

		// logger.info("get json path data");

		long startTime2 = System.currentTimeMillis();
		JsonNode jsonNode = null;
		try {
			jsonNode = objectMapper.readValue(jsonData, JsonNode.class);
			// jsonNode = objectMapper.readTree(jsonData);
			long endTime2 = System.currentTimeMillis();

			// System.err.println("time 2 : " + (endTime2 - startTime2));
		} catch (JsonProcessingException e) {
			logger.error("error during parsing string to json using jsonNode and object with erro message [{}]",
					e.getMessage());
			throw new IntegrationFrameworkGeneralException(
					"error during parsing string to json using jsonNode and object with erro message [" + e.getMessage()
							+ "]");
		} catch (IOException e) {
			logger.error("error during parsing string to json using jsonNode and object with erro message [{}]",
					e.getMessage());
			throw new IntegrationFrameworkGeneralException(
					"error during parsing string to json using jsonNode and object with erro message [" + e.getMessage()
							+ "]");
		}

		Object result = null;
		try {
			result = (Object) jsonNode.at(expression);
			result = StringUtils.replace(result.toString(), "\"", "");
		} catch (Exception e) {
			return "";
		}

		return result.toString();
	}
	/*
	 * set data from xml to json
	 */


	public String setDataFromXmlToBusMsg(String businessMessage,String path, String xmlResponse) {

		long startTime3 = System.currentTimeMillis();

		JSONObject jsonObject = XML.toJSONObject(xmlResponse);

		JsonNode jsonNode = null;
		try {
			jsonNode = objectMapper.readValue(jsonObject.toString(), JsonNode.class);
		} catch (JsonProcessingException e) {
			logger.error("error during parsing json with error message [{}]", e.getMessage());
		} catch (IOException e) {
			logger.error("error during parsing json with error message [{}]", e.getMessage());
		}

		ObjectNode objectNode =null;
		try {
			objectNode = (ObjectNode) jsonNode.at(PATH_OF_BODY);
		}catch (Exception ex){
			objectNode = (ObjectNode) jsonNode;
		}
		try {
			jsonNode = objectMapper.readValue(businessMessage, JsonNode.class);
		} catch (JsonProcessingException e) {
			logger.error("error during parsing json with error message [{}]", e.getMessage());
		} catch (IOException e) {
			logger.error("error during parsing json with error message [{}]", e.getMessage());
		}
		//((ObjectNode) jsonNode.get(EXTRA_FIELDS)).setAll(objectNode);
		((ObjectNode) jsonNode.get(EXTRA_FIELDS)).put(path,objectNode);
		long endTime3 = System.currentTimeMillis();
		// System.err.println("time 3: " + (endTime3 - startTime3));
		return jsonNode.toString();
	}

	public String setDataFromXmlToBusMsg(String businessMessage, String xmlResponse) {

		long startTime3 = System.currentTimeMillis();

		JSONObject jsonObject = XML.toJSONObject(xmlResponse);

		JsonNode jsonNode = null;
		try {
			jsonNode = objectMapper.readValue(jsonObject.toString(), JsonNode.class);
		} catch (JsonProcessingException e) {
			logger.error("error during parsing json with error message [{}]", e.getMessage());
		} catch (IOException e) {
			logger.error("error during parsing json with error message [{}]", e.getMessage());
		}

		ObjectNode objectNode = (ObjectNode) jsonNode.at(PATH_OF_BODY);
		try {
			jsonNode = objectMapper.readValue(businessMessage, JsonNode.class);
		} catch (JsonProcessingException e) {
			logger.error("error during parsing json with error message [{}]", e.getMessage());
		} catch (IOException e) {
			logger.error("error during parsing json with error message [{}]", e.getMessage());
		}
		((ObjectNode) jsonNode.get(EXTRA_FIELDS)).setAll(objectNode);
		long endTime3 = System.currentTimeMillis();
		// System.err.println("time 3: " + (endTime3 - startTime3));
		return jsonNode.toString();
	}

	/*
	 * assign object node to business message
	 */

	private String setJsonPathDataToJson(String json, String path, Object value) {

		String updatedJson = null;

		try {
			updatedJson = JsonPath.using(configuration).parse(json).set(JSON_PATH_SYMBOL + path, value).json()
					.toString();
		} catch (PathNotFoundException e) {
			logger.error("json path not found in [{}]", path);
			throw new IntegrationFrameworkGeneralException("json path [" + path + "] not found in busMsg");
		}

		return updatedJson;
	}


	public String setBusMsgafterReturnXMLResponse(String businessMessage, ResponseOfClient responseOfClient, Map<String,String> listOfPaths,
												   String serviceName, String clientName) {

		String removePrefixes = Common.removePrefixs(responseOfClient.getResponse());

		Node nodeOfErrorCode = null;
		Node nodeOfDescription = null;

		String busMsg = businessMessage;

		if (responseOfClient.getContentTypeResponse().toLowerCase().contains(XML_NAME)) {

			Document doc = null;

			try {
				doc = Common.parseDocument(removePrefixes);
			} catch (IntegrationFrameworkGeneralException e) {

			}
			String xmlNode = "";
			for (Map.Entry<String, String> path : listOfPaths.entrySet()) {
				try {
					String pathCode = StringUtils.replace(path.getKey(), DOT, SLASH);
					pathCode = StringUtils.replace(pathCode, serviceName + SLASH, "");

					if (StringUtils.isNotBlank(pathCode)) {
						nodeOfErrorCode = (Node) Common.returnxPath(doc, DOUBLE_SLASH + pathCode, Node.class);

						xmlNode = nodeOfErrorCode != null ? Common.transformNodeToXMLString(nodeOfErrorCode):"";
					}


					if (path.getValue().contains(EXTRA_FIELDS) && xmlNode != null && !xmlNode.equals("")) {

						businessMessage = setDataFromXmlToBusMsg(businessMessage,path.getValue().substring(path.getValue().indexOf('.')+1),  xmlNode);
					}
					else if (nodeOfErrorCode != null && !nodeOfErrorCode.getTextContent().equals("")) {
						businessMessage = setJsonPathDataToJson(businessMessage, path.getValue(), nodeOfErrorCode.getTextContent());
						//businessMessage = setDataFromXmlToBusMsg(businessMessage, removePrefixes);

					}
					} catch(IntegrationFrameworkGeneralException e){
						throw e;
					}

				}

			}



		return businessMessage;
	}




	public String setBusMsgafterReturnJSONResponse(String businessMessage, ResponseOfClient responseOfClient, Map<String,String> listOfPaths,
												   String serviceName, String clientName) {

		if (responseOfClient.getContentTypeResponse().toLowerCase().contains(JSON)) {

			String pathVal="";
			for (Map.Entry<String,String> path : listOfPaths.entrySet()) {
				try {
					String JSONPath = StringUtils.replace(path.getKey(), serviceName , "");
					JSONPath = StringUtils.replace(JSONPath, DOT, SLASH);
					JSONPath = StringUtils.replace(JSONPath, serviceName + SLASH, "");
					if (StringUtils.isNotBlank(JSONPath)) {
						pathVal = getJsonPathDataFromJson2(JSONPath,responseOfClient.getResponse());
					}
					if (path.getValue().contains(EXTRA_FIELDS) && !pathVal.equals("")) {
						businessMessage = AddDataFromJsonToBusMsg(businessMessage, path.getValue().substring(path.getValue().indexOf('.')+1),pathVal);
					}
					else if (pathVal != null && !pathVal.equals("")) {
						businessMessage = setJsonPathDataToJson(businessMessage, path.getValue(), pathVal);

					}
				} catch (IntegrationFrameworkGeneralException e) {
					throw e;
				}

			}
		}
		return businessMessage;
	}


	public String setDataFromBusMsgToJsonString(String businessMessage, List<String> listOfPaths, String request,
												String serviceName, String clientName) {

		String value = "";
		for (String path : listOfPaths) {
			try {
				value = setDataOfJson(serviceName + DOT + path, businessMessage, clientName);
				request = setJsonPathDataToJson(request, path, value);
				// start fix added by ibrahim Osama 1/5/2019
				if (value ==null || (value !=null && value.equals("")) ){
					throw new IntegrationFrameworkGeneralException("xpath "+path+ " is not mapped correctly please add a valid mapping path");
				}
				// end fix added by ibrahim Osama 1/5/2019
			} catch (IntegrationFrameworkGeneralException e) {
				throw e;
			}

		}

		return request;
	}

	public String AddDataFromJsonToBusMsg(String businessMessage, Object key,Object value) {

		JsonNode jsonNode = null;
		JsonNode responseNode = null;

		ObjectNode objectNode = (ObjectNode) jsonNode;

		try {
			jsonNode = objectMapper.readTree(businessMessage);
			responseNode = objectMapper.readTree(value.toString());
		} catch (JsonProcessingException e) {
			logger.error("error during parsing json with error message [{}]", e.getMessage());
		} catch (IOException e) {
			logger.error("error during parsing json with error message [{}]", e.getMessage());
		}
		((ObjectNode) jsonNode.get(EXTRA_FIELDS)).put(key.toString(),responseNode);

		return jsonNode.toString();
	}


	/*
	 * set data from json to busMsg
	 */
	public String setDataFromJsonToBusMsg(String businessMessage, String jsonResponse) {

		JsonNode jsonNode = null;

		try {
			jsonNode = objectMapper.readTree(jsonResponse);
		} catch (JsonProcessingException e) {
			logger.error("error during parsing json with error message [{}]", e.getMessage());
		} catch (IOException e) {
			logger.error("error during parsing json with error message [{}]", e.getMessage());

		}

		ObjectNode objectNode = (ObjectNode) jsonNode;

		try {
			jsonNode = objectMapper.readTree(businessMessage);
		} catch (JsonProcessingException e) {
			logger.error("error during parsing json with error message [{}]", e.getMessage());
		} catch (IOException e) {
			logger.error("error during parsing json with error message [{}]", e.getMessage());
		}
		((ObjectNode) jsonNode.get(EXTRA_FIELDS)).setAll(objectNode);

		return jsonNode.toString();
	}

	public String mapErrorCodesInXmlResponse(ResponseOfClient responseOfClient, String pathOfErrorCode,
											 String pathOfDescription, String businessMessage, String clientName, String serviceName) {

		String removePrefixes = Common.removePrefixs(responseOfClient.getResponse());

		Node nodeOfErrorCode = null;
		Node nodeOfDescription = null;

		String busMsg = businessMessage;

		if (responseOfClient.getContentTypeResponse().toLowerCase().contains(XML_NAME)) {

			Document doc = null;

			try {
				doc = Common.parseDocument(removePrefixes);
			} catch (IntegrationFrameworkGeneralException e) {

			}
			String pathCode = StringUtils.replace(pathOfErrorCode, DOT, SLASH);
			pathCode = StringUtils.replace(pathCode, serviceName + SLASH, "");
			try {
				if (StringUtils.isNotBlank(pathCode)) {
					nodeOfErrorCode = (Node) Common.returnxPath(doc, DOUBLE_SLASH + pathCode, Node.class);
				}
				String pathDesc = StringUtils.replace(pathOfDescription, DOT, SLASH);
				pathDesc = StringUtils.replace(pathDesc, serviceName + SLASH, "");
				if (StringUtils.isNotBlank(pathDesc)) {
					nodeOfDescription = (Node) Common.returnxPath(doc, DOUBLE_SLASH + pathDesc, Node.class);
				}
			} catch (IntegrationFrameworkGeneralException e) {

			}
		}

		String errCode = null;

		if (nodeOfErrorCode != null && nodeOfDescription != null && !nodeOfDescription.getTextContent().equals("")) {

			if (dynamicMappingService.getMapOfErrorCodeOfClient().get(clientName + "-" + "errorCodes").containsKey(
					//ERROR_CODE + nodeOfErrorCode.getTextContent() + DOT + nodeOfDescription.getTextContent())) {
					ERROR_CODE + nodeOfErrorCode.getTextContent() )) {
				errCode = dynamicMappingService.getMapOfErrorCodeOfClient().get(clientName + "-" + "errorCodes")
						//.get(ERROR_CODE + nodeOfErrorCode.getTextContent() + DOT + nodeOfDescription.getTextContent())
						.get(ERROR_CODE + nodeOfErrorCode.getTextContent())
						.toString();
				try {
					busMsg = setJsonPathDataToJson(busMsg, ERROR_FLAG_BUS_MSG, true);
					busMsg = setJsonPathDataToJson(busMsg, ERROR_CODE_BUS_MSG, errCode);
				} catch (IntegrationFrameworkGeneralException e) {
					throw e;
				}
				return busMsg;
			}
		} else if (nodeOfErrorCode != null && !nodeOfErrorCode.getTextContent().equals("")) {
			if (dynamicMappingService.getMapOfErrorCodeOfClient().get(clientName + "-" + "errorCodes")
					.containsKey(ERROR_CODE + nodeOfErrorCode.getTextContent())) {
				errCode = dynamicMappingService.getMapOfErrorCodeOfClient().get(clientName + "-" + "errorCodes")
						.get(ERROR_CODE + nodeOfErrorCode.getTextContent()).toString();
				try {
					busMsg = setJsonPathDataToJson(busMsg, ERROR_FLAG_BUS_MSG, true);
					busMsg = setJsonPathDataToJson(busMsg, ERROR_CODE_BUS_MSG, errCode);
				} catch (IntegrationFrameworkGeneralException e) {
					throw e;
				}
				return busMsg;
			}
		} else if (nodeOfDescription != null && !nodeOfDescription.getTextContent().equals("")) {
			try {
				if (dynamicMappingService.getMapOfErrorCodeOfClient().get(clientName + "-" + "errorCodes").containsKey(ERROR_CODE + nodeOfDescription.getTextContent())) {
					errCode = dynamicMappingService.getMapOfErrorCodeOfClient().get(clientName + "-" + "errorCodes").get(ERROR_CODE + nodeOfDescription.getTextContent()).toString();
				}else {
					errCode = dynamicMappingService.getMapOfErrorCodeOfClient().get(clientName + "-" + "errorCodes")
							.get(DEFAULT_ERROR_CODE).toString();
				}

				busMsg = setJsonPathDataToJson(busMsg, ERROR_FLAG_BUS_MSG, true);
				busMsg = setJsonPathDataToJson(busMsg, ERROR_CODE_BUS_MSG, errCode);
			} catch (IntegrationFrameworkGeneralException e) {
				throw e;
			}
			return busMsg;

		} else {// if there is description we must add it in BusMsg here
			if (responseOfClient.getStatusCodeValue() != 200) {
				if (StringUtils.isNotBlank(responseOfClient.getErrorCode())) {// this condition for checking that
					// hystrix return response
					errCode = responseOfClient.getErrorCode();
					try {
						busMsg = setJsonPathDataToJson(busMsg, ERROR_FLAG_BUS_MSG, true);
						busMsg = setJsonPathDataToJson(busMsg, ERROR_CODE_BUS_MSG, errCode);
					} catch (IntegrationFrameworkGeneralException e) {
						throw e;
					}
				} else {
					errCode = dynamicMappingService.getMapOfErrorCodeOfClient().get(clientName + "-" + "errorCodes")
							.get(DEFAULT_ERROR_CODE).toString();
					try {
						busMsg = setJsonPathDataToJson(busMsg, ERROR_FLAG_BUS_MSG, true);
						busMsg = setJsonPathDataToJson(busMsg, ERROR_CODE_BUS_MSG, errCode);
					} catch (IntegrationFrameworkGeneralException e) {
						throw e;
					}
				}
				return busMsg;
			}
		}

		// businessMessage = setJsonPathDataToJson(businessMessage,
		// ERROR_FLAG_BUS_MSG, false);
		long startTime3 = System.currentTimeMillis();
		//busMsg = setDataFromXmlToBusMsg(businessMessage, removePrefixes);
		busMsg = mapXMLResponseInBusMsg( businessMessage, responseOfClient,  clientName,  serviceName);
		long endTime3 = System.currentTimeMillis();

		// System.err.println("time 3: " + (endTime3 - startTime3));
		return busMsg;
	}

	public String mapErrorCodesInJsonResponse(ResponseOfClient responseOfClient, String pathOfErrorCode,
											  String pathOfDescription, String businessMessage, String serviceName, String clientName)throws IntegrationFrameworkGeneralException {

		String resultCode = null;
		String resultDesc = null;

		String busMsg = businessMessage;

		if (responseOfClient.getContentTypeResponse().toLowerCase().contains(JSON)) {
			String pathCode = StringUtils.replace(pathOfErrorCode, serviceName /* + DOT */, "");
			pathCode = StringUtils.replace(pathCode, DOT, SLASH);
			pathCode = StringUtils.replace(pathCode, serviceName + SLASH, "");
			try {
				if (StringUtils.isNotBlank(pathCode)) {
					resultCode = getJsonPathDataFromJson(/* JSON_PATH_SYMBOL +*/ pathCode,
							responseOfClient.getResponse());
				}

				String pathDesc = StringUtils.replace(pathOfDescription, serviceName /* + DOT */, "");
				pathDesc = StringUtils.replace(pathDesc, DOT, SLASH);
				pathDesc = StringUtils.replace(pathDesc, serviceName + SLASH, "");
				if (StringUtils.isNotBlank(pathDesc)) {
					resultDesc = getJsonPathDataFromJson(/* JSON_PATH_SYMBOL + */pathDesc,
							responseOfClient.getResponse());
				}
			} catch (IntegrationFrameworkGeneralException e) {

			}

		}

		String errCode = null;

		if (resultCode != null && resultDesc != null && !resultDesc.equals("")) {
			if (dynamicMappingService.getMapOfErrorCodeOfClient().get(clientName + "-" + "errorCodes")
					.containsKey(ERROR_CODE + resultCode + DOT + resultDesc)) {
				errCode = dynamicMappingService.getMapOfErrorCodeOfClient().get(clientName + "-" + "errorCodes")
						.get(ERROR_CODE + resultCode + DOT + resultDesc).toString();
				try {
					busMsg = setJsonPathDataToJson(busMsg, ERROR_FLAG_BUS_MSG, true);
					busMsg = setJsonPathDataToJson(busMsg, ERROR_CODE_BUS_MSG, errCode);
				} catch (IntegrationFrameworkGeneralException e) {
					throw e;
				}
				return busMsg;
			}
		} else if (resultCode != null && !resultCode.equals("")) {
			if (dynamicMappingService.getMapOfErrorCodeOfClient().get(clientName + "-" + "errorCodes")
					.containsKey(ERROR_CODE + resultCode)) {
				errCode = dynamicMappingService.getMapOfErrorCodeOfClient().get(clientName + "-" + "errorCodes")
						.get(ERROR_CODE + resultCode).toString();
			}else {
				errCode = dynamicMappingService.getMapOfErrorCodeOfClient().get(clientName + "-" + "errorCodes")
						.get(DEFAULT_ERROR_CODE).toString();
			}
			busMsg = setJsonPathDataToJson(busMsg, ERROR_FLAG_BUS_MSG, true);
			busMsg = setJsonPathDataToJson(busMsg, ERROR_CODE_BUS_MSG, errCode);
			return busMsg;

		} else if (resultDesc != null && !resultDesc.equals("")) {
			if (dynamicMappingService.getMapOfErrorCodeOfClient().get(clientName + "-" + "errorCodes")
					.containsKey(ERROR_CODE + resultDesc)) {
				errCode = dynamicMappingService.getMapOfErrorCodeOfClient().get(clientName + "-" + "errorCodes")
						.get(ERROR_CODE + resultDesc).toString();
				try {
					busMsg = setJsonPathDataToJson(busMsg, ERROR_FLAG_BUS_MSG, true);
					busMsg = setJsonPathDataToJson(busMsg, ERROR_CODE_BUS_MSG, errCode);
				} catch (IntegrationFrameworkGeneralException e) {
					throw e;
				}
				return busMsg;
			}
		} else {
			if (responseOfClient.getStatusCodeValue() != 200) {
				try {
					if(responseOfClient.getStatusCodeValue()==1 || responseOfClient.getStatusCodeValue()==2)
						errCode=responseOfClient.getErrorCode();
					else {
						try
						{
							errCode = dynamicMappingService.getMapOfErrorCodeOfClient().get(clientName + "-" + "errorCodes").get(DEFAULT_ERROR_CODE).toString();

						}catch (Exception ex){
							errCode="IFW02";
						}

					}
					busMsg = setJsonPathDataToJson(busMsg, ERROR_FLAG_BUS_MSG, true);
					busMsg = setJsonPathDataToJson(busMsg, ERROR_CODE_BUS_MSG, errCode);
				} catch (Exception e) {
					throw new IntegrationFrameworkGeneralException(e.getMessage());
				}
				return busMsg;
			}
		}
		try {
			busMsg = setJsonPathDataToJson(busMsg, ERROR_FLAG_BUS_MSG, false);
			busMsg = mapJSONResponseInBusMsg(busMsg,responseOfClient,clientName,serviceName);
		//	busMsg = setDataFromJsonToBusMsg(busMsg, responseOfClient.getResponse());
		} catch (IntegrationFrameworkGeneralException e) {
			throw e;
		}
		return busMsg;
	}

	public String mapXMLResponseInBusMsg(String businessMessage, ResponseOfClient responseOfClient, String clientName, String serviceName)
	{

		Map<String,String> listOfPaths=new HashMap<String, String>();
		Set<Map.Entry<Object, Object>> clientResponse=dynamicMappingService.getMapOfVericashAndClientResponse().get(clientName).entrySet();
		for (Map.Entry<Object, Object> val:clientResponse) {
			if(val.getKey().toString().contains(serviceName+DOT))
			{
				listOfPaths.put(val.getKey().toString(),val.getValue().toString());
			}
		}


		return  setBusMsgafterReturnXMLResponse(businessMessage,responseOfClient,listOfPaths,serviceName, clientName);
	}

	public String mapJSONResponseInBusMsg(String businessMessage, ResponseOfClient responseOfClient, String clientName, String serviceName)
	{

		Map<String,String> listOfPaths=new HashMap<String, String>();
		Set<Map.Entry<Object, Object>> clientResponse=dynamicMappingService.getMapOfVericashAndClientResponse().get(clientName).entrySet();
		for (Map.Entry<Object, Object> val:clientResponse) {
			if(val.getKey().toString().contains(serviceName))
			{
				listOfPaths.put(val.getKey().toString(),val.getValue().toString());
			}
		}


		return setBusMsgafterReturnJSONResponse(businessMessage,responseOfClient,listOfPaths,serviceName, clientName);
	}
	public String setParamDataInRestUrl(String businessMessage, List<String> listOfParam, String url,
										String clientName) {

		String value = "";

		for (String path : listOfParam) {
			try {
				value = setDataOfJson(path, businessMessage, clientName);
			} catch (IntegrationFrameworkGeneralException e) {
				throw e;
			}
			url = url.replaceAll(REGEX_LEFT_CURLY_BRACE + Common.splitString(path, DOT)[2] + REGEX_RIGHT_CURLY_BRACE,
					value);
		}

		return url;
	}

		private String setDataOfJson(String path, String businessMessage, String clientName) {

		String extraFieldsExpression = /* JSON_PATH_SYMBOL */ SLASH + EXTRA_FIELDS + SLASH/* DOT */;
		String value = "";

		String pathAfterReplaceDotWithSlash = StringUtils.replace(path, DOT, SLASH);

		if (dynamicMappingService.getMapOfVericashAndClient().get(clientName).containsKey(path)) {
			String replaceDotWithSlash = SLASH + StringUtils.replace(
					dynamicMappingService.getMapOfVericashAndClient().get(clientName).get(path).toString(), DOT, SLASH);
			//String searchInCommonMapExpression = replaceDotWithSlash;

			try {
				value = getJsonPathDataFromJson(replaceDotWithSlash, businessMessage);
			} catch (IntegrationFrameworkGeneralException e) {
				throw e;
			}

		} else {
			String searchInExtraFieldExpression = extraFieldsExpression + pathAfterReplaceDotWithSlash;
			try {
				value = getJsonPathDataFromJson(searchInExtraFieldExpression, businessMessage);
			} catch (IntegrationFrameworkGeneralException e) {
				throw e;
			}
		}
		return value;
	}
}
