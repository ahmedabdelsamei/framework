package com.cit.vc.service;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Map;
import java.util.function.Function;

import javax.net.ssl.SSLContext;

import com.cit.vc.exceptions.IntegrationFrameworkGeneralException;
import com.netflix.hystrix.exception.HystrixRuntimeException;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.cit.vc.model.HystrixConfig;
import com.cit.vc.model.HystrixPojo;
import com.cit.vc.model.MappingRequest;
import com.cit.vc.model.MessageHeader;
import com.cit.vc.model.ResponseOfClient;
import com.cit.vc.utils.HystrixFallBackCommand;
import com.google.gson.Gson;


@Component
public class DynamicClientService {

	private static final Logger logger = LoggerFactory.getLogger("filelogger");
	
	//private static final String SOAP_ACTION = "SOAPAction";
	//private static final String CONTENT_TYPE = "Content-Type";
	//private static final String CONTENTTYPE = "ContentType";
	//private static final String METHOD_TYPE = "method-type";
	private static final String HYSTRIX = "-hystrix.json";
	
	@Autowired
	private ListenOnFilesProperties listenOnFilesProperties;
	
	@Autowired
	private RestTemplate restTemplate;

	/*@Autowired
	private AsyncRestTemplate asyncRestTemplate;*/
	
	
	
	public ResponseOfClient sendDynamicRequestAndRecieveResponse(MappingRequest mappingRequest, MessageHeader messageHeader) {

		String request= mappingRequest.getRequest();
		Map<String, String> headersOfRequest=mappingRequest.getHeaders();
		String methodType=mappingRequest.getMethodType();
		String url = mappingRequest.getUrl();
		boolean enableSSL = mappingRequest.isEnableSSL();
		String certificatePath = mappingRequest.getCertificatePath();
		String keypassword = mappingRequest.getKeypassword();
		
		HystrixConfig hystrixConfig = new Gson().fromJson(listenOnFilesProperties.getSaveAllFileProperties().get(messageHeader.getServiceId() + HYSTRIX), HystrixConfig.class);

		String response = null;
		
		HystrixPojo hystrixPojo = new HystrixPojo();
		
		hystrixPojo.setEnableSSL(enableSSL);
		hystrixPojo.setCertificateParh(certificatePath);
		hystrixPojo.setKeyPassword(keypassword);
		hystrixPojo.setInternalCall(true);
		hystrixPojo.setUrl(url);
		hystrixPojo.setMessageHeader(messageHeader);
		hystrixPojo.setRequest(request);
		hystrixPojo.setHeaders(headersOfRequest);
		hystrixPojo.setHystrixConfig(hystrixConfig);
		hystrixPojo.setMethodType(methodType);
		
		response = executeHystrixCommand(hystrixPojo);

		ResponseOfClient responseOfClient = new Gson().fromJson(response, ResponseOfClient.class);

		
		/*HttpHeaders headers = new HttpHeaders();
		headers.add(SOAP_ACTION, hystrixPojo.getHeaders().get(SOAP_ACTION));
		headers.add(CONTENT_TYPE, hystrixPojo.getHeaders().get(CONTENTTYPE));
		
		HttpEntity<String> httpEntity = new HttpEntity<String>(request, headers);
		
		
		  ListenableFuture<ResponseEntity<String>> entity = asyncRestTemplate.exchange(url, HttpMethod.valueOf(hystrixPojo.getHeaders().get(METHOD_TYPE).toUpperCase()), httpEntity, String.class);
		 
		  	        entity.addCallback(new ListenableFutureCallback<ResponseEntity<String>>() {

						@Override
						public void onSuccess(ResponseEntity<String> result) {
							// TODO Auto-generated method stub
							//System.err.println("request is " + request + " and response is " + result.getBody());
						}

						@Override
						public void onFailure(Throwable ex) {
							// TODO Auto-generated method stub
							
						}
		  
		  
		  	        });
*/		
		
		
		
		
		  	        
//		responseOfClient = new Gson().fromJson(response, ResponseOfClient.class);
//		logger.info("responseOfClient is [{}]", responseOfClient.getResponse());

		
		/*HttpHeaders headers = new HttpHeaders();
		headers.add(SOAP_ACTION, hystrixPojo.getHeaders().get(SOAP_ACTION));
		headers.add(CONTENT_TYPE, hystrixPojo.getHeaders().get(CONTENTTYPE));
		
		HttpEntity<String> httpEntity = new HttpEntity<String>(request, headers);

		ResponseEntity<String> res = null;
		ResponseOfClient responseOfClient = new ResponseOfClient();
		

		
		try {
			long startTime = System.currentTimeMillis();
			res = restTemplate.exchange(url, HttpMethod.valueOf(hystrixPojo.getHeaders().get(METHOD_TYPE).toUpperCase()), httpEntity, String.class);
			long endTime = System.currentTimeMillis();
			System.err.println("time: " + (endTime - startTime));
			responseOfClient.setStatusCodeValue(res.getStatusCodeValue());
			responseOfClient.setResponse(replaceSymbols(res.getBody()));
			responseOfClient.setContentTypeResponse(res.getHeaders().getContentType().getSubtype());
		} catch (HttpStatusCodeException e) {
			
			logger.error("there is an exception while calling third party [{}]", e.getMessage());
			responseOfClient.setStatusCodeValue(e.getStatusCode().value());
			responseOfClient.setDescriptionMessage(e.getStatusText());
			responseOfClient.setResponse(e.getResponseBodyAsString());
			responseOfClient.setContentTypeResponse(e.getResponseHeaders().getContentType().getSubtype());
		}*/

		/*response.setResponse(replaceSymbols("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + 
				"<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" + 
				"    <soap:Body>\n" + 
				"        <GetTrxResponse xmlns=\"http://craftsilicon.com/\">\n" + 
				"            <GetTrxResult>&lt;GetTransaction&gt;&lt;Status&gt;SUCCESS&lt;/Status&gt;&lt;Message&gt;MANUALPROCESS&lt;/Message&gt;&lt;/GetTransaction&gt;</GetTrxResult>\n" + 
				"        </GetTrxResponse>\n" + 
				"    </soap:Body>\n" + 
				"</soap:Envelope>"));
		response.setStatusCodeValue(200);*/
		
		return responseOfClient;
	}

	
	Function<HystrixPojo, String> fun = (new Function<HystrixPojo, String>() {
		public String apply(HystrixPojo hystrixPojo) { // The object
			return passFunction(hystrixPojo); // The method
		}
	});

	public String passFunction(HystrixPojo hystrixPojo) {
		 
		HttpHeaders httpHeaders = new HttpHeaders();
		//httpHeaders.add(URL, hystrixPojo.getUrl());
		//httpHeaders.add(SOAP_ACTION, hystrixPojo.getHeaders().get(SOAP_ACTION));
		//httpHeaders.add(CONTENT_TYPE, hystrixPojo.getHeaders().get(CONTENTTYPE));
		//httpHeaders.add(METHOD_TYPE, hystrixPojo.getHeaders().get(METHOD_TYPE));
		httpHeaders.setAll(hystrixPojo.getHeaders());
		HttpEntity<String> httpEntity = new HttpEntity<String>(hystrixPojo.getRequest(), httpHeaders);
		hystrixPojo.setInternalCall(false);
		
		ResponseEntity<String> res = null;
		ResponseOfClient responseOfClient = new ResponseOfClient();
		String response = null;
		
		try {
			long startTime = System.currentTimeMillis();
			boolean enableSSL = hystrixPojo.isEnableSSL();
			
			
			if(enableSSL)
			{
				String certificatePath = hystrixPojo.getCertificateParh();
				String keypassword = hystrixPojo.getKeyPassword();
				KeyStore keyStore = null;
				try {
					keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
					keyStore.load(new FileInputStream(new File(certificatePath)), keypassword.toCharArray());
					SSLContext context = new SSLContextBuilder().loadTrustMaterial(null, new TrustSelfSignedStrategy()).loadKeyMaterial(keyStore, keypassword.toCharArray()).build();
					SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(context , NoopHostnameVerifier.INSTANCE);
					HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory).build();
					ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
					RestTemplate restTemplate = new RestTemplate(requestFactory);
					res = restTemplate.exchange(hystrixPojo.getUrl(), HttpMethod.valueOf(hystrixPojo.getMethodType().toUpperCase()), httpEntity, String.class);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else
			{
				res = restTemplate.exchange(hystrixPojo.getUrl(), HttpMethod.valueOf(hystrixPojo.getMethodType().toUpperCase()), httpEntity, String.class);
			}
			long endTime = System.currentTimeMillis();
			//System.err.println("time 2: " + (endTime - startTime));
			responseOfClient.setStatusCodeValue(res.getStatusCodeValue());
			responseOfClient.setResponse(replaceSymbols(res.getBody()));
			responseOfClient.setContentTypeResponse(res.getHeaders().getContentType().getSubtype());
		}catch (HttpStatusCodeException e) {
			
			logger.error("there is an exception while calling third party [{}]", e.getMessage());
			responseOfClient.setStatusCodeValue(e.getStatusCode().value());
			responseOfClient.setDescriptionMessage(e.getStatusText());
			responseOfClient.setResponse(e.getResponseBodyAsString());
			responseOfClient.setContentTypeResponse(e.getResponseHeaders().getContentType().getSubtype());
			hystrixPojo.setResponseInCaseOfException(new Gson().toJson(responseOfClient));
		}catch (ResourceAccessException ex){
			logger.error("hystrix is not enabled while calling third party [{}]", ex.getMessage());
			throw new IntegrationFrameworkGeneralException("hystrix is not enabled while calling third party and the target host is not avaliable");
		}
		response = new Gson().toJson(responseOfClient);
		//hystrixPojo.setResponseInCaseOfException(response);
		return response;
	}

	public String executeHystrixCommand(HystrixPojo hystrixPojo) {
		HystrixFallBackCommand hystrixFallBackCommand ;
		if(hystrixPojo.getHystrixConfig()!=null)
			  hystrixFallBackCommand = new HystrixFallBackCommand(fun, hystrixPojo);
		else
			 hystrixFallBackCommand = new HystrixFallBackCommand(fun, hystrixPojo,"defaultConfig");
		String response = hystrixFallBackCommand.execute();
		return response;
	}

	private String replaceSymbols(String response) {

		String stringUtils = StringUtils.replace(response, "&lt;", "<");
		stringUtils = StringUtils.replace(stringUtils, "&gt;", ">");
		return stringUtils;
	}

}
