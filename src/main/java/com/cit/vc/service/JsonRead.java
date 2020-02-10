package com.cit.vc.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cit.vc.utils.Common;
import com.cit.vc.utils.JsonParser;
import com.cit.vc.exceptions.IntegrationFrameworkGeneralException;
import com.cit.vc.service.ListenOnFilesProperties;

@Component
public class JsonRead {

	private static final Logger logger = LoggerFactory.getLogger("filelogger");

	private static final String QUESTION_MARK = "?";
	private static final String AMPERSAND = "&";
	private static final String SLASH = "/";
	private static final String PARAM = ".param.";
	private static final String DOT = ".";
	private static final String DASH = "-";

	private Map<String, List<String>> cachePathsOfJson = new ConcurrentHashMap<String, List<String>>();

	@Autowired
	private ListenOnFilesProperties listenOnFilesProperties;

	@Autowired
	private DataSetToJsonOrXmlString dataSetToJsonOrXmlString;

	public String returnJsonRequestAfterSetData(String requestPath, String businessMessage, String serviceName,
			String clientName) {

		String requestData = null;
		requestData = listenOnFilesProperties.getSaveAllFileProperties().get(requestPath);

		String request = null;
		String serviceId = clientName + DASH + serviceName;
		List<String> listOfJsonPaths = new CopyOnWriteArrayList<String>();

		 if (!cachePathsOfJson.containsKey(serviceId)) {
			
			 cachePathsOfJson.put(serviceId, listOfJsonPaths);
			
			 try {
				JsonParser jsonParser = new JsonParser(requestData, listOfJsonPaths);
				//listOfJsonPaths = jsonParser.getPathList();
			} catch (Exception e) {
				throw new IntegrationFrameworkGeneralException(
						"error during parsing json and get all list paths with error [" + e.getMessage() + "]");
			}

			if (listOfJsonPaths.isEmpty()) {
				throw new IntegrationFrameworkGeneralException("there are no paths in json request to set data");
			}
		}else {
//			if (cachePathsOfJson.containsKey(serviceId)) {
				listOfJsonPaths = cachePathsOfJson.get(serviceId);
//			}
		}

		try {
			request = dataSetToJsonOrXmlString.setDataFromBusMsgToJsonString(businessMessage, listOfJsonPaths,
					requestData, serviceName, clientName);
		} catch (IntegrationFrameworkGeneralException e) {
			throw e;
		}
		return request;
	}

	// maybe decouple params list and body paths list
	public String returnRestUrlAfterSetParams(String businessMessage, String url, String serviceName,
			String clientName) {

		List<String> listOfParam = getListOfParamPaths(url, serviceName);
		try {
			url = dataSetToJsonOrXmlString.setParamDataInRestUrl(businessMessage, listOfParam, url, clientName);
		} catch (IntegrationFrameworkGeneralException e) {
			throw e;
		}
		return url;
	}

	private List<String> getListOfParamPaths(String url, String serviceName) {

		String[] split;
		List<String> list = new ArrayList<String>();
		if (url.contains(QUESTION_MARK)) {
			url = url.substring(url.indexOf(QUESTION_MARK) + 1, url.length());
			split = url.split(AMPERSAND);

		} else {
			split = url.split(SLASH);
		}

		for (int i = 0; i < split.length; ++i) {
			if (Common.isCurlyBracesExist(split[i])) {
				String param = Common.getDataBetweenCurlyBraces(split[i]);
				String path = serviceName + PARAM + param;
				list.add(path);
			}
		}
		return list;

	}

	/*
	 * maybe this method will change
	 */
	public List<String> getRequestPaths(String request, String url, String serviceName) {
		List<String> listOfJsonPaths = new ArrayList<String>();
		if (request != null) {
			JsonParser jsonParser = new JsonParser(request, listOfJsonPaths);

			listOfJsonPaths = addServiceNameInPath(jsonParser.getPathList(), serviceName);
		}
		listOfJsonPaths.addAll(getListOfParamPaths(url, serviceName));

		return listOfJsonPaths;
	}

	private List<String> addServiceNameInPath(List<String> listOfPaths, String serviceName) {
		List<String> list = new ArrayList<String>();

		for (String path : listOfPaths) {
			list.add(serviceName + DOT + path);
		}
		return list;
	}

}
