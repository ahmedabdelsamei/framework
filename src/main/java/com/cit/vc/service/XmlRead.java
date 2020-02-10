package com.cit.vc.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.cit.vc.utils.Common;
import com.cit.vc.exceptions.IntegrationFrameworkGeneralException;
import com.cit.vc.service.ListenOnFilesProperties;

@Component
public class XmlRead {

	@Autowired
	private DataSetToJsonOrXmlString dataSetToJsonOrXmlString;

	@Autowired
	private ListenOnFilesProperties listenOnFilesProperties;

	private static final Logger logger = LoggerFactory.getLogger("filelogger");

	private static final String SLASH = "/";
	private static final String DOUBLE_SLASH = "//";
	private static final String ASTERISK = "*";
	private static final String BODY = "Body";
	private static final String HEADER = "Header";
	private static final String DASH = "-";
	
	private Map<String, List<String>> cacheRequestPaths = new ConcurrentHashMap<String, List<String>>();

	/*
	 * return request after set data to its parameters
	 */

	public String returnXmlRequestAfterSetData(String pathOfRequest, String serviceId, String businessMessage) {

		String[] splitClientAndServiceName = Common.splitString(serviceId, DASH);
		
		String clientName = splitClientAndServiceName[0];

		String serviceName = splitClientAndServiceName[1];
		
		String requestData = listenOnFilesProperties.getSaveAllFileProperties().get(pathOfRequest);

		Document document = null;
		try {
			document = Common.parseDocument(requestData);
		} catch (IntegrationFrameworkGeneralException e) {
			throw e;
		}

		List<String> listOfPaths = new CopyOnWriteArrayList<String>();
		long startTime = System.currentTimeMillis();

		if (!cacheRequestPaths.containsKey(serviceId)) {

			cacheRequestPaths.put(serviceId, listOfPaths);

			long startTime2 = System.currentTimeMillis();

			try {
				listOfPaths = readXml(listOfPaths, document);
			} catch (IntegrationFrameworkGeneralException e) {
				throw e;
			}

			long endTime2 = System.currentTimeMillis();
			// System.err.println("time 2 : " + (endTime2 - startTime2));

		} else {
			listOfPaths = cacheRequestPaths.get(serviceId);
		}
		long endTime = System.currentTimeMillis();
		// System.err.println("time 1 : " + (endTime - startTime));

		long startTime2 = System.currentTimeMillis();
		for (String path : listOfPaths) {
			dataSetToJsonOrXmlString.setElementsData(document, path, businessMessage,
					clientName, serviceName);
		}
		long endTime2 = System.currentTimeMillis();

		// System.err.println("time 2 : " + (endTime2 - startTime2));

		long startTime3 = System.currentTimeMillis();
		String request = null;
		try {
			request = Common.transformDocumentToString(document);
		} catch (IntegrationFrameworkGeneralException e) {
			throw e;
		}
		long endTime3 = System.currentTimeMillis();

		// System.err.println("time 3 : " + (endTime3 - startTime3));

		return request;

	}

	/*
	 * return response after set data to business message
	 */

	public String returnBussMsgAfterSetData(String businessMessage, String xmlResposne) {

		return dataSetToJsonOrXmlString.setDataFromXmlToBusMsg(businessMessage, xmlResposne);

	}

	public List<String> getRequestPaths(String request, List<String> listOfPaths) {

		Document document = Common.parseDocument(request);
		listOfPaths = readXml(listOfPaths, document);
		return listOfPaths;

	}
	/*
	 * read XML request
	 */

	private List<String> readXml(List<String> listOfPaths, Document document) {

		long startTime = System.currentTimeMillis();
		NodeList nodeList = null;
		String expression = DOUBLE_SLASH + BODY;

		for (int j = 1; j <= 2; ++j) {
			String expr = expression + SLASH + ASTERISK;

			try {
				nodeList = (NodeList) Common.returnxPath(document, expr, NodeList.class);
			} catch (IntegrationFrameworkGeneralException e) {
				throw e;
			}
			if (nodeList == null) {
				throw new IntegrationFrameworkGeneralException("there are no paths to get in document xml");
			}
			long endTime = System.currentTimeMillis();

			// System.err.println("time 1 : " + (endTime - startTime));
			for (int i = 0; i < nodeList.getLength(); i++) {

				Node nNode = nodeList.item(i);

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					listOfPaths = recurXmlRequest(nNode, /* xPath, */ expression + SLASH + nNode.getNodeName(),
							document, listOfPaths);
				}

			}
			expression = DOUBLE_SLASH + HEADER;
		}
		return listOfPaths;

	}

	/*
	 * recursion on request to get all paths
	 */

	private List<String> recurXmlRequest(Node node, /* XPath xPath, */ String expression, Document document,
			List<String> listOfPaths) {
		if (node.getChildNodes().getLength() == 0) {
			listOfPaths.add(expression);
			return listOfPaths;
		} else if (node.getChildNodes().getLength() == 1 && node.getFirstChild().getNodeType() == Node.TEXT_NODE) {

			listOfPaths.add(expression);
			return listOfPaths;

		} else {

			for (int i = 0; i < node.getChildNodes().getLength(); ++i) {
				Node nNode = node.getChildNodes().item(i);

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					expression += SLASH + nNode.getNodeName();
					recurXmlRequest(nNode, /* xPath, */ expression, document, listOfPaths);
					expression = /* StringUtils.replace(expression, SLASH + nNode.getNodeName(), ""); */expression
							.substring(0, expression.lastIndexOf("/"));
				}
			}

		}
		return listOfPaths;
	}

}
