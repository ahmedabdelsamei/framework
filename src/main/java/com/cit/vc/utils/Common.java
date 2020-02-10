package com.cit.vc.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.cit.vc.exceptions.IntegrationFrameworkGeneralException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

public class Common {

	static ObjectMapper mapper = new ObjectMapper();

	private static final Logger logger = LoggerFactory.getLogger("filelogger");

	private static XPath xPath = XPathFactory.newInstance().newXPath();
	private static TransformerFactory transformerFactory = TransformerFactory.newInstance();
	private static final String SLASH = "/";
	private static final String DOUBLE_SLASH = "//";
	private static final String REGEX_OF_CURLY_BRACES = "\\{.*?\\}";

	public static boolean validate(String jsonString, String schemaString) throws Exception {

		final JsonNode jsonSchema = JsonLoader.fromString(schemaString);

		final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();

		final JsonSchema schema = factory.getJsonSchema(jsonSchema);

		JsonNode json = mapper.readTree(jsonString);

		ProcessingReport report = schema.validate(json);

		if (report != null) {
			return report.isSuccess();
		}

		return false;

	}

	/*
	 * remove prefixes
	 */

	public static String removePrefixs(String xmlString) {
		return xmlString.replaceAll("(<\\?[^<]*\\?>)?", ""). /* remove preamble */
				replaceAll("xmlns.*?(\"|\').*?(\"|\')", "") /* remove xmlns declaration */
				.replaceAll("(<)(\\w+:)(.*?>)", "$1$3") /* remove opening tag prefix */
				.replaceAll("(</)(\\w+:)(.*?>)", "$1$3") /* remove closing tags prefix */
				.replaceAll("(<\\w+)(\\s+\\w+:nil=\"true\".*)(/>)","$1$3"); /* remove nil="true" */
	}

	/*
	 * for check if there is parameters in rest url between curly braces
	 */

	public static String returnRestUrlAfterReplaceParameter(String url, String replaceString) {

		return url.replaceAll(REGEX_OF_CURLY_BRACES, replaceString);
	}

	/*
	 * for check if there is parameters in rest url between curly braces
	 */

	public static boolean isCurlyBracesExist(String url) {
		Pattern p = Pattern.compile(REGEX_OF_CURLY_BRACES); // the pattern to search for
		Matcher m = p.matcher(url);
		if (m.find())
			return true;
		return false;

	}

	public static String getDataBetweenCurlyBraces(String data) {

		Pattern p = Pattern.compile(REGEX_OF_CURLY_BRACES); // the pattern to search for
		Matcher m = p.matcher(data);
		if (m.find()) {
			return m.group().toString().replaceAll("\\{|\\}", "");
		}
		return "";

	}
	/*
	 * convert URL to String
	 */

	/*public static String convertUrlToString(String url) {

		String urlToString = null;
		try {
			urlToString = IOUtils.toString(new URL(url), "UTF-8");
		} catch (MalformedURLException e) {
			logger.error("cannot convert url to string with error message [{}]", e.getMessage());
			throw new IntegrationFrameworkGeneralException(
					"cannot convert url to string with error message [" + e.getMessage() + "]");
		} catch (IOException e) {
			logger.error("cannot convert url to string with error message [{}]", e.getMessage());
			throw new IntegrationFrameworkGeneralException(
					"cannot convert url to string with error message [" + e.getMessage() + "]");
		}

		return urlToString;
	}*/

	/*
	 * read property files
	 */

/*	public static Properties readFileProperties(String fileLocation) {

		InputStream file = null;
		try {
			file = new ClassPathResource(fileLocation).getInputStream();
		} catch (IOException e) {
			logger.error("cannot read file and put it into properties with error message [{}]", e.getMessage());
			throw new IntegrationFrameworkGeneralException(
					"cannot read file and put it into properties with error message [" + e.getMessage() + "]");
		}

		Properties prop = new Properties();

		Reader fileReader = new InputStreamReader(file);

		// Always wrap FileReader in BufferedReader.
		BufferedReader bufferedReader = new BufferedReader(fileReader);

		try {
			prop.load(bufferedReader);
		} catch (IOException e) {
			logger.error("cannot read file and put it into properties with error message [{}]", e.getMessage());
			throw new IntegrationFrameworkGeneralException(
					"cannot read file and put it into properties with error message [" + e.getMessage() + "]");
		}

		return prop;

	}*/

	/*
	 * parse XML Document
	 */

	public static Document parseDocument(String result) {

		Document document = null;
		InputStream stream = new ByteArrayInputStream(result.getBytes(StandardCharsets.UTF_8));
		try {

			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);
		} catch (SAXException e) {
			logger.error("error during parse string to xml document with error message [{}]", e.getMessage());
			throw new IntegrationFrameworkGeneralException(
					"error during parse string to xml document with error message [" + e.getMessage() + "]");

		} catch (IOException e) {
			logger.error("error during parse string to xml document with error message [{}]", e.getMessage());
			throw new IntegrationFrameworkGeneralException(
					"error during parse string to xml document with error message [" + e.getMessage() + "]");

		} catch (ParserConfigurationException e) {
			logger.error("error during parse string to xml document with error message [{}]", e.getMessage());
			throw new IntegrationFrameworkGeneralException(
					"error during parse string to xml document with error message [" + e.getMessage() + "]");
		}
		return document;
	}

	/*
	 * get XPATH of XML
	 */

	public static Object returnxPath(Document document, String expression, Object returnType) {

		long startTime = System.currentTimeMillis();

		try {
			if (returnType.equals(NodeList.class)) {
				returnType = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
			} else if (returnType.equals(Node.class)) {
				returnType = (Node) xPath.compile(expression).evaluate(document, XPathConstants.NODE);
			} else {
				returnType = (String) xPath.compile(expression).evaluate(document, XPathConstants.STRING);
			}

		} catch (XPathExpressionException e) {
			logger.error("error in xPath expression [{}]", e.getMessage());
			throw new IntegrationFrameworkGeneralException(
					"error in xPath expression with error message [" + e.getMessage() + "]");
		}
		long endTime = System.currentTimeMillis();

		// System.err.println("time 1 : " + (endTime - startTime));
		return returnType;
	}

	public static String transformNodeToXMLString(Node node)
	{
		Transformer transformer = null;
		StreamResult xmlOutput = new StreamResult(new StringWriter());
		try {
			transformer = transformerFactory.newTransformer();
		} catch (TransformerConfigurationException e) {
			logger.error("error during transform xml node to string [{}]", e.getMessage());
			throw new IntegrationFrameworkGeneralException(
					"error during transform xml node to string with error message [" + e.getMessage() + "]");
		}

		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

		try {
			transformer.transform(new DOMSource(node), xmlOutput);
		} catch (TransformerException e) {
			logger.error("error during transform xml Node to string [{}]", e.getMessage());
			throw new IntegrationFrameworkGeneralException(
					"error during transform xml Node to string with error message [" + e.getMessage() + "]");
		}


		String nodeAsAString = xmlOutput.getWriter().toString();

		return nodeAsAString;
	}


	/*
	 * transform XML document to String
	 */

	public static String transformDocumentToString(Document doc) {

		Transformer transformer = null;
		try {
			transformer = transformerFactory.newTransformer();
		} catch (TransformerConfigurationException e) {
			logger.error("error during transform xml document to string [{}]", e.getMessage());
			throw new IntegrationFrameworkGeneralException(
					"error during transform xml document to string with error message [" + e.getMessage() + "]");
		}
		StringWriter writer = new StringWriter();

		try {
			transformer.transform(new DOMSource(doc), new StreamResult(writer));
		} catch (TransformerException e) {
			logger.error("error during transform xml document to string [{}]", e.getMessage());
			throw new IntegrationFrameworkGeneralException(
					"error during transform xml document to string with error message [" + e.getMessage() + "]");
		}

		String xmlString = writer.getBuffer().toString();

		return xmlString;
	}

	public static String[] splitString(String string, String symbol) {
		String[] split = string.split("\\" + symbol);
		return split;
	}

	/*
	 * replace double slashes and slashes
	 */

	public static String removeSlashes(String data, String replaceSymbol) {

		String replaceDoubleSlash = StringUtils.replace(data, DOUBLE_SLASH, "");
		String replaceSlashWithDot = StringUtils.replace(replaceDoubleSlash, SLASH, replaceSymbol);
		return replaceSlashWithDot;
	}

	/*
	 * split with DOT
	 
	public static String[] splitPath(String path, String symbol) {

		String[] splitString = path.split("\\" + symbol);

		return splitString;
	}*/

	/*
	 * convert string to property
	 */
	public static Properties returnProperties(String string) {
		Properties prop = new Properties();
		try {
			prop.load(new StringReader(string));
		} catch (IOException e) {
			logger.error("cannot convert string to properties");
			throw new IntegrationFrameworkGeneralException(
					"cannot convert string to properties [" + e.getMessage() + "]");
		}
		return prop;
	}

	private static void writeToFile(String path, String key, String data) {

		File file = new File(path);
		PropertiesConfiguration config = null;
		try {
			config = new PropertiesConfiguration(file);
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		config.setProperty(key, data);
		try {
			config.save();
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
