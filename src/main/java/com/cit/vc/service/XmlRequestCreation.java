package com.cit.vc.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;

import org.apache.commons.io.FileExistsException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.WSDLParser;
import com.predic8.wstool.creator.RequestTemplateCreator;
import com.predic8.wstool.creator.SOARequestCreator;

import groovy.xml.MarkupBuilder;
//import io.undertow.protocols.http2.StreamErrorException;

@Component
public class XmlRequestCreation {

	private static final String SLASH = "/";
	private static final String DASH = "-";
	private static final String XML_EXTENSION = ".xml";
	private static final String REPLACE_SYMBOLS = "?XXX?";

	@Autowired
	private ListenOnFilesProperties listenOnFilesProperties;

	private static final Logger logger = LoggerFactory.getLogger("filelogger");

	// For test and must change
	// private static Map<String,String> storeSoapActionForEachService = new
	// HashMap<String,String>();

	// public static Map<String, String> getStoreSoapActionForEachService() {
	// return storeSoapActionForEachService;
	// }

	public String createRequest(String url, String clientName, String serviceName, String path) {

		logger.info("create soap request ....");
		Definitions wsdl = returnDefinition(url);
		StringWriter writer = new StringWriter();
		String portTypeName = wsdl.getPortTypes().get(0).getName();
		String bindingName = wsdl.getBindings().get(0).getName();
		// BindingOperation bindingOperation =
		// wsdl.getBinding(bindingName).getOperation(serviceName);
		// String soapAction = (bindingOperation.getOperation().getSoapAction());
		// soapAction = StringUtils.replace(soapAction, SLASH + serviceName, "");
		// storeSoapActionForEachService.put(clientName + DASH + serviceName, soapAction
		// + SLASH + serviceName);
		SOARequestCreator creator = new SOARequestCreator(wsdl, new RequestTemplateCreator(),
				new MarkupBuilder(writer));

		creator.createRequest(portTypeName, serviceName, bindingName);

		String pathOfNewFile = path + SLASH + clientName + SLASH + clientName + DASH + serviceName + XML_EXTENSION;
		String request = StringUtils.replace(writer.toString(), REPLACE_SYMBOLS, "");
		logger.info("request after creating it is : [{}]", request);

		// if request is empty
		if (StringUtils.isNotBlank(request)) {
			listenOnFilesProperties.getSaveAllFileProperties().put(clientName + DASH + serviceName + XML_EXTENSION,
					request);
			writeToFile(pathOfNewFile, request);
		}
		return request;
	}

	private void writeToFile(String path, String request) {
		OutputStream os = null;
		try {
			os = new FileOutputStream(new File(path));
			os.write(request.getBytes(), 0, request.length());
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error with write to file null outputstream: " + e.getMessage());
		} finally {
			try {
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private Definitions returnDefinition(String url) {

		WSDLParser parser = new WSDLParser();
		Definitions def = parser.parse(url);
		return def;
	}

}
