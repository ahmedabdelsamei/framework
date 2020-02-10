package com.cit.vc.service;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Time;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Component;

import com.cit.vc.exceptions.IntegrationFrameworkGeneralException;
import com.cit.vc.model.MappingRequest;
import com.cit.vc.model.ResponseOfClient;
import com.cit.vc.utils.Common;
import com.cit.vc.service.ListenOnFilesProperties;
import com.google.gson.Gson;

@Component
public class DynamicMappingService {

	@Value("${path.File}")
	private String path;

	@Autowired
	private XmlRead xmlRead;

	@Autowired
	private JsonRead jsonRead;

	@Autowired
	private DataSetToJsonOrXmlString dataSetToJsonOrXmlString;

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	private ListenOnFilesProperties listenOnFilesProperties;

	private static final Logger logger = LoggerFactory.getLogger("filelogger");

	private static final String ALL = "/*";
	private static final String FILE = "file:";
	private static final String BODY = "Body";
	private static final String DOT = ".";
	private static final String VERICASH_FILE_PROPERTIES = "vericash.properties";
	private static final String PROPERTIES = ".properties";
	private static final String RESPONSE = "-response";
	private static final String JSON = ".json";
	private static final String XML = ".xml";
	private static final String EXTRA_FIELDS = "extraFields.";
	private static final String URL = ".url";
	private static final String ENABLE_SSL = ".enableSSL";
	private static final String CERTIFICATE_PATH = ".ssl.certificate.path";
	private static final String KEY_PASSWORD = ".ssl.certificate.keypassword";
	private static final String HEADER = "header";
	private static final String WSDL_URL = "wsdlUrl";
	private static final String DASH = "-";
	private static final String ERROR_CODES = "errorCodes";
	private static final String METHOD_TYPE = ".method-type";
	
	private Map<String, Map<Object, Object>> mapClientProperties = new ConcurrentHashMap<String, Map<Object, Object>>();
	private Map<Object, Object> mapVericashProperties = new HashMap<Object, Object>();
	private Map<String, Map<Object, Object>> mapOfVericashAndClient = new ConcurrentHashMap<String, Map<Object, Object>>();
	private Map<String, Map<Object, Object>> mapOfErrorCodeOfClient = new ConcurrentHashMap<String, Map<Object, Object>>();
	private Map<String, Map<String, String>> storeErrorCodeAndDescOfEachService = new ConcurrentHashMap<String, Map<String, String>>();
	private Map<String, Map<Object, Object>> mapClientResponse = new ConcurrentHashMap<String, Map<Object, Object>>();
	private Map<String, Map<Object, Object>> mapOfVericashAndClientResponse = new ConcurrentHashMap<String, Map<Object, Object>>();

	public Map<String, Map<Object, Object>> getMapOfVericashAndClientResponse() {
		return mapOfVericashAndClientResponse;
	}

	public void setMapOfVericashAndClientResponse(Map<String, Map<Object, Object>> mapOfVericashAndClientResponse) {
		this.mapOfVericashAndClientResponse = mapOfVericashAndClientResponse;
	}

	public Map<String, Map<Object, Object>> getMapClientResponse() {
		return mapClientResponse;
	}

	public void setMapClientResponse(Map<String, Map<Object, Object>> mapClientResponse) {
		this.mapClientResponse = mapClientResponse;
	}

	public Map<String, Map<Object, Object>> getMapClientProperties() {
		return mapClientProperties;
	}

	public Map<Object, Object> getMapVericashProperties() {
		return mapVericashProperties;
	}

	public void setMapVericashProperties(Map<Object, Object> mapVericashProperties) {
		this.mapVericashProperties = mapVericashProperties;
	}

	public Map<String, Map<Object, Object>> getMapOfVericashAndClient() {
		return mapOfVericashAndClient;
	}

	public Map<String, Map<Object, Object>> getMapOfErrorCodeOfClient() {
		return mapOfErrorCodeOfClient;
	}

	public MappingRequest getRequest(String businessMessage, String serviceId,String correlationId) {

		MappingRequest mappingRequest = new MappingRequest();

		
		String[] splitClientAndServiceName = Common.splitString(serviceId, DASH);
		String clientName = splitClientAndServiceName[0];

		String serviceName = splitClientAndServiceName[1];

		fillMapClientProperties(clientName);

		String wsdlUrl = null;
		String urlOfService = null;
		boolean enableSSL=false;
		String certificatePath=null;
		String keypassword=null;
		try
		{
			enableSSL=new Boolean(mapClientProperties.get(clientName).get(serviceName + ENABLE_SSL).toString());
			certificatePath = mapClientProperties.get(clientName).get(serviceName + CERTIFICATE_PATH).toString();
			keypassword = mapClientProperties.get(clientName).get(serviceName + KEY_PASSWORD).toString();
			mappingRequest.setEnableSSL(enableSSL);
			mappingRequest.setCertificatePath(certificatePath);
			mappingRequest.setKeypassword(keypassword);
			
		}catch(Exception e)
		{
			enableSSL=false;
		}
		String methodType = null;

		try {
			wsdlUrl = mapClientProperties.get(clientName).get(WSDL_URL).toString();
		} catch (NullPointerException e) {
			logger.warn("wsdlUrl param is not set in the confogiration file "+clientName + ".properties");
			//throw new IntegrationFrameworkGeneralException("wsdlUrl param is not set in the confogiration file "+clientName + ".properties");
		}

		try {
			urlOfService = mapClientProperties.get(clientName).get(serviceName + URL).toString();
		} catch (NullPointerException e) {
			logger.warn("urlOfService param is not set in the confogiration file "+clientName + ".properties");
			//throw new IntegrationFrameworkGeneralException("urlOfService param is not set in the confogiration file "+clientName + ".properties");
		}

		try {
			methodType = mapClientProperties.get(clientName).get(serviceName + METHOD_TYPE).toString();
		} catch (NullPointerException e) {
			logger.error("methodType param is not set in the confogiration file "+clientName + ".properties");
			throw new IntegrationFrameworkGeneralException("methodType param is not set in the confogiration file "+clientName + ".properties");
		}
		
		if (StringUtils.isAllBlank(wsdlUrl, urlOfService, methodType)) {
			throw new IntegrationFrameworkGeneralException("wsdlUrl, urlOfService or methodType is null or empty");
		}

		String pathOfXmlFile = serviceId + XML;
		String pathOfJsonFile = serviceId + JSON;
		String request = null;

		if (StringUtils.isNotBlank(wsdlUrl)) {

			long timeStart = System.currentTimeMillis();

			try {
				request = xmlRead.returnXmlRequestAfterSetData(pathOfXmlFile, serviceId, businessMessage);
			} catch (IntegrationFrameworkGeneralException e) {
				throw e;
			}
			long timeEnd = System.currentTimeMillis();

			// System.err.println("time is : " + (timeEnd - timeStart));

			mappingRequest.setUrl(wsdlUrl);
		} else if (StringUtils.isNotBlank(urlOfService)
				&& listenOnFilesProperties.getSaveAllFileProperties().containsKey(pathOfXmlFile)) {

			try {
				request = xmlRead.returnXmlRequestAfterSetData(pathOfXmlFile, serviceId, businessMessage);
				urlOfService = jsonRead.returnRestUrlAfterSetParams(businessMessage, urlOfService, serviceName,
						clientName);
			} catch (IntegrationFrameworkGeneralException e) {
				throw e;
			}

			mappingRequest.setUrl(urlOfService);

		} else if (StringUtils.isNotBlank(urlOfService)
				&& listenOnFilesProperties.getSaveAllFileProperties().containsKey(pathOfJsonFile)) {

			try {
				request = jsonRead.returnJsonRequestAfterSetData(pathOfJsonFile, businessMessage, serviceName,
						clientName);
				urlOfService = jsonRead.returnRestUrlAfterSetParams(businessMessage, urlOfService, serviceName,
						clientName);
			} catch (IntegrationFrameworkGeneralException e) {
				throw e;
			}

			mappingRequest.setUrl(urlOfService);

		} else {
			try {
				urlOfService = jsonRead.returnRestUrlAfterSetParams(businessMessage, urlOfService, serviceName,
						clientName);
			} catch (IntegrationFrameworkGeneralException e) {
				throw e;
			}
			mappingRequest.setUrl(urlOfService);
		}
		
		
		mappingRequest.setMethodType(methodType);
		mappingRequest.setRequest(request);
		mappingRequest.setHeaders(getHeadersFromClientProperties(clientName, serviceName));

		logger.info("Mapping request with serviceId  [{}] and correlationId [{}] is \n [{}]", serviceId,correlationId, mappingRequest.toString());
		return mappingRequest;
	}

	private Map<String, String> getHeadersFromClientProperties(String clientName, String serviceName) {

		Map<String, String> headers = new HashMap<String, String>();
		for (Entry<Object, Object> entry : mapClientProperties.get(clientName).entrySet()) {

			String key = entry.getKey().toString();

			if (key.contains(HEADER)) {
				String[] split = Common.splitString(key, DOT);
				if (split[0].contains(HEADER)) {
					headers.put(split[1], entry.getValue().toString());
				} else {
					if (split[0].equals(serviceName)) {
						headers.put(split[2], entry.getValue().toString());
					}
				}
			}
		}
		return headers;

	}

	@PostConstruct
	private void loadFiles() {

		Map<String, String> saveFilePropertiesForFirstTime = new HashMap<String, String>();
		Resource[] directories = null;
		try {
			directories = loadResources(FILE + path + ALL);

			for (Resource directory : directories) {

				String path = directory.getFile().getPath();

				Resource[] files = loadResources(FILE + path + ALL);

				for (Resource file : files) {

					String fileName = file.getFilename();

					String dataOfFile = IOUtils.toString(file.getInputStream());

					saveFilePropertiesForFirstTime.put(fileName, dataOfFile);
				}

			}
		} catch (IOException e) {
			throw new IntegrationFrameworkGeneralException(
					"there is an error of load files on startup : " + e.getMessage());
		}
		if (saveFilePropertiesForFirstTime.isEmpty()) {
			throw new IntegrationFrameworkGeneralException("there is no files to fetch data from it");
		}
		listenOnFilesProperties.setSaveAllFileProperties(saveFilePropertiesForFirstTime);

		mapVericashProperties = Common
				.returnProperties(listenOnFilesProperties.getSaveAllFileProperties().get(VERICASH_FILE_PROPERTIES));
	}

	private Resource[] loadResources(String pattern) throws IOException {
		return ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(pattern);
	}

	private void fillMapClientResponseProperties(String clientName) {

		if (!mapClientResponse.containsKey(clientName+RESPONSE)) {
			try {
				String clientPropertyData = listenOnFilesProperties.getSaveAllFileProperties()
						.get(clientName +RESPONSE+ PROPERTIES);
				if(clientPropertyData ==null) {
					String errorMsg="File " + clientName + RESPONSE + PROPERTIES + " doesn't exist, Please create it.";
					logger.error(errorMsg);
					throw new IntegrationFrameworkGeneralException(errorMsg);

				}
				Properties prop=Common.returnProperties(clientPropertyData);
				if(prop.size()==0)
					logger.warn("There is no response mapping in the file "+ clientName+RESPONSE + PROPERTIES);
				mapClientResponse.put(clientName+RESPONSE, prop);
				mapBetweenVericashAndClientResponse(clientName);

			} catch (IntegrationFrameworkGeneralException e) {
				throw e;
			}
		}

	}


	private void fillMapClientProperties(String clientName) {

		if (!mapClientProperties.containsKey(clientName)) {
			try {
				String clientPropertyData = listenOnFilesProperties.getSaveAllFileProperties()
						.get(clientName + PROPERTIES);
				mapClientProperties.put(clientName, Common.returnProperties(clientPropertyData));
				mapBetweenVericashAndClients(clientName);

			} catch (IntegrationFrameworkGeneralException e) {
				throw e;
			}
		}

	}

	private void mapBetweenVericashAndClientResponse(String clientName) {

		mapOfVericashAndClientResponse.put(clientName, new ConcurrentHashMap<Object, Object>());
		for (Object object : mapClientResponse.get(clientName+RESPONSE).keySet()) {
			if (mapVericashProperties.containsKey(mapClientResponse.get(clientName+RESPONSE).get(object))) {
				mapOfVericashAndClientResponse.get(clientName).put(object.toString(),
						mapVericashProperties.get(mapClientResponse.get(clientName+RESPONSE).get(object)));
			}else if(mapClientResponse.get(clientName+RESPONSE).get(object).toString().contains(EXTRA_FIELDS)){
				mapOfVericashAndClientResponse.get(clientName).put(object.toString(),mapClientResponse.get(clientName+RESPONSE).get(object));
			}
		}
	}

	private void mapBetweenVericashAndClients(String clientName) {

		mapOfVericashAndClient.put(clientName, new ConcurrentHashMap<Object, Object>());
		for (Object object : mapClientProperties.get(clientName).keySet()) {
			if (mapVericashProperties.containsKey(mapClientProperties.get(clientName).get(object))) {
				mapOfVericashAndClient.get(clientName).put(object.toString(),
						mapVericashProperties.get(mapClientProperties.get(clientName).get(object)));
			}
		}
	}

	private void fillErrorCodeAndDescOfEachServiceProperties(String serviceId){
		String pathOfErrorCode = null;
		String pathOfDescription = null;
		String[] splitServiceId = Common.splitString(serviceId, DASH);
		String clientName = splitServiceId[0];
		String serviceName = splitServiceId[1];
		String keyOfErrorCodeFile = clientName + DASH + ERROR_CODES;
		if (!mapOfErrorCodeOfClient.containsKey(keyOfErrorCodeFile)) {
			try {
				String errorFileName=listenOnFilesProperties.getSaveAllFileProperties().get(keyOfErrorCodeFile + PROPERTIES);
				if(errorFileName==null)
					throw new IntegrationFrameworkGeneralException("error-codes mapping file does not exist ");
				mapOfErrorCodeOfClient.put(keyOfErrorCodeFile, Common.returnProperties(errorFileName));
			} catch (IntegrationFrameworkGeneralException e) {
				throw e;
			}
		}

		if (storeErrorCodeAndDescOfEachService.containsKey(serviceId)) {
			pathOfErrorCode = storeErrorCodeAndDescOfEachService.get(serviceId).get("errorCode");
			pathOfDescription = storeErrorCodeAndDescOfEachService.get(serviceId).get("errorDesc");
		} else {
			Map<String, String> map = new HashMap<String, String>();
			try {
				Set<Map.Entry<Object, Object>> keyVal=mapClientResponse.get(clientName+RESPONSE).entrySet();
				for (Entry<Object, Object> val:keyVal)
				{
					if (val.getKey().toString().contains(serviceName)&&val.getValue().equals("errorCode"))
					{
						pathOfErrorCode= val.getKey().toString();
						break;
					}
				}
				for (Entry<Object, Object> val:keyVal)
				{
					if (val.getKey().toString().contains(serviceName)&&val.getValue().equals("errorDesc"))
					{
						pathOfDescription= val.getKey().toString();
						break;
					}
				}

				map.put("errorCode", pathOfErrorCode);
				map.put("errorDesc", pathOfDescription);
			}catch(NullPointerException e) {
				e.printStackTrace();
			}

			storeErrorCodeAndDescOfEachService.put(serviceId, map);
		}

	}

	/*
	 * response prepare
	 */
	public String getResponse(String businessMessage, ResponseOfClient responseOfClient, String serviceId)
	{

		logger.info("response with serviceId [{}] is {}", serviceId, responseOfClient.getResponse());

		String[] splitServiceId = Common.splitString(serviceId, DASH);
		String clientName = splitServiceId[0];
		String serviceName = splitServiceId[1];
		fillMapClientResponseProperties(clientName);
		fillErrorCodeAndDescOfEachServiceProperties(serviceId);
		String busMsgResponse = null;

		boolean checkForXml = listenOnFilesProperties.getSaveAllFileProperties().containsKey(serviceId + XML);

		String pathOfErrorCode = storeErrorCodeAndDescOfEachService.get(serviceId).get("errorCode");
		String pathOfDescription =  storeErrorCodeAndDescOfEachService.get(serviceId).get("errorDesc");



		if (checkForXml) {
			long startTime3 = System.currentTimeMillis();
			try {
				busMsgResponse = dataSetToJsonOrXmlString.mapErrorCodesInXmlResponse(responseOfClient, pathOfErrorCode,
						pathOfDescription, businessMessage, clientName, serviceName);
			} catch (IntegrationFrameworkGeneralException e) {
				throw e;
			}

		} else {
			try {
				busMsgResponse = dataSetToJsonOrXmlString.mapErrorCodesInJsonResponse(responseOfClient, pathOfErrorCode,pathOfDescription, businessMessage, serviceName, clientName);
			} catch (IntegrationFrameworkGeneralException e) {
				throw e;
			}
		}

		return busMsgResponse;
	}

	public String getRequestStructure(String serviceId) {

		String[] splitClientAndServiceName = Common.splitString(serviceId, DASH);
		String clientName = splitClientAndServiceName[0];

		String serviceName = splitClientAndServiceName[1];

		List<String> paths = new ArrayList<String>();
		fillMapClientProperties(clientName);
		Map<String, String> map = new HashMap<String, String>();

		Map<Object, Object> mapOfClientProp = Common
				.returnProperties(listenOnFilesProperties.getSaveAllFileProperties().get(clientName + PROPERTIES));

		String request = listenOnFilesProperties.getSaveAllFileProperties().get(clientName + DASH + serviceName + XML);

		if (request == null) {
			request = listenOnFilesProperties.getSaveAllFileProperties().get(clientName + DASH + serviceName + JSON);
			paths = jsonRead.getRequestPaths(request, mapOfClientProp.get(serviceName + URL).toString(), serviceName);
		} else {
			paths = xmlRead.getRequestPaths(request, paths);
		}

		for (String path : paths) {

			String pathAfterRemoveBody = StringUtils.replace(Common.removeSlashes(path, DOT), BODY + DOT, "");

			if (mapOfVericashAndClient.get(clientName).containsKey(pathAfterRemoveBody)) {
				map.put(mapClientProperties.get(clientName).get(pathAfterRemoveBody).toString(),
						mapOfVericashAndClient.get(clientName).get(pathAfterRemoveBody).toString());
			} else {
				int len = Common.splitString(pathAfterRemoveBody, DOT).length;
				String key = Common.splitString(pathAfterRemoveBody, DOT)[len - 1];
				map.put(key, EXTRA_FIELDS + pathAfterRemoveBody);
			}
		}

		Gson gson = new Gson();
		String json = gson.toJson(map);
		return json;
	}

}
