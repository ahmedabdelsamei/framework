package com.cit.vc.jms;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import com.cit.vc.model.MessageHeader;
import com.cit.vc.utils.Headers;

@Component
public class JmsPublisher {

	private static final Logger logger = LoggerFactory.getLogger("filelogger");

	@Autowired
	private JmsTemplate jmsTemplate;

	@Value("${log.queue}")
	private String logQueue;

	public void send(String logMessage) {

		TextMessage textMessage = null;
		try {
			textMessage = jmsTemplate.getConnectionFactory().createConnection()
					.createSession(false, Session.AUTO_ACKNOWLEDGE).createTextMessage();
			textMessage.setText(logMessage);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		jmsTemplate.convertAndSend(logQueue, textMessage);
	}
	
	public void sendResponse(String queueName, String response, MessageHeader messageHeader) {
		TextMessage textMessage = null;
		try {
			textMessage = jmsTemplate.getConnectionFactory().createConnection()
					.createSession(false, Session.AUTO_ACKNOWLEDGE).createTextMessage();
			textMessage.setText(response);
			textMessage.setJMSCorrelationID(messageHeader.getCorrelationId());
			//textMessage.setStringProperty(Headers.CHANNEL.name(), messageHeader.getChannel());
			textMessage.setStringProperty(Headers.SERVICE_ID.name(), messageHeader.getServiceId());
			//textMessage.setIntProperty(Headers.STEP_ORDER.name(), messageHeader.getStepOrder());
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		jmsTemplate.convertAndSend(queueName, textMessage);
	}
	
	//for testing 
	public void sendRequest() {
		String request = "{\r\n" + 
				"\r\n" + 
				"  \"header\": {\r\n" + 
				"\r\n" + 
				"    \"instanceUniqueID\": \"26ffc379f67211e78fabf7b632e28ee4\"\r\n" + 
				"\r\n" + 
				"  },\r\n" + 
				"\r\n" + 
				"  \"serviceInfo\": {\r\n" + 
				"\r\n" + 
				"    \"id\": \"16139776\",\r\n" + 
				"\r\n" + 
				"    \"code\": \"45011\",\r\n" + 
				"\r\n" + 
				"    \"stepId\": \"1000054\",\r\n" + 
				"\r\n" + 
				"    \"businessServiceInfo\": {\r\n" + 
				"\r\n" + 
				"      \"businessServiceSteps\": [\r\n" + 
				"\r\n" + 
				"        {\r\n" + 
				"\r\n" + 
				"          \"stepOrder\": 1,\r\n" + 
				"\r\n" + 
				"          \"txnDefId\": 16143023,\r\n" + 
				"\r\n" + 
				"          \"type\": \"Transaction\",\r\n" + 
				"\r\n" + 
				"          \"stepId\": 16145029,\r\n" + 
				"\r\n" + 
				"          \"serviceMode\": \"Normal\"\r\n" + 
				"\r\n" + 
				"        },\r\n" + 
				"\r\n" + 
				"        {\r\n" + 
				"\r\n" + 
				"          \"stepOrder\": 2,\r\n" + 
				"\r\n" + 
				"          \"txnDefId\": 0,\r\n" + 
				"\r\n" + 
				"          \"serviceId\": \"1000054\",\r\n" + 
				"\r\n" + 
				"          \"type\": \"Service\",\r\n" + 
				"\r\n" + 
				"          \"serviceStack\": \"EXTERNAL\",\r\n" + 
				"\r\n" + 
				"          \"stepId\": 16139777,\r\n" + 
				"\r\n" + 
				"          \"serviceMode\": \"Normal\"\r\n" + 
				"\r\n" + 
				"        }\r\n" + 
				"\r\n" + 
				"      ]\r\n" + 
				"\r\n" + 
				"    }\r\n" + 
				"\r\n" + 
				"  },\r\n" + 
				"\r\n" + 
				"  \"primarySenderInfo\": {\r\n" + 
				"\r\n" + 
				"    \"userKey\": \"1234+2348071236545\",\r\n" + 
				"\r\n" + 
				"    \"msisdn\": \"+2348071236545\",\r\n" + 
				"\r\n" + 
				"    \"imei\": \"00000000-0200-5562-9381-362193815461\",\r\n" + 
				"\r\n" + 
				"    \"swk\": \"qNpypFcnSDE4buqmNi6amw==\",\r\n" + 
				"\r\n" + 
				"    \"password\": \"Cit12345\",\r\n" + 
				"\r\n" + 
				"    \"ownerType\": \"CUSTOMER\",\r\n" + 
				"\r\n" + 
				"    \"balance\": 0,\r\n" + 
				"\r\n" + 
				"    \"fee\": 0,\r\n" + 
				"\r\n" + 
				"    \"entityInfo\": {},\r\n" + 
				"\r\n" + 
				"    \"personalDetails\": {\r\n" + 
				"\r\n" + 
				"      \"firstName\": \"Samar\",\r\n" + 
				"\r\n" + 
				"      \"middleName\": \"Ahmed\",\r\n" + 
				"\r\n" + 
				"      \"lastName\": \"cor\",\r\n" + 
				"\r\n" + 
				"      \"gender\": \"Male\",\r\n" + 
				"\r\n" + 
				"      \"languageId\": 0\r\n" + 
				"\r\n" + 
				"    },\r\n" + 
				"\r\n" + 
				"    \"paymentMethod\": {\r\n" + 
				"\r\n" + 
				"      \"paymentMethodType\": 0,\r\n" + 
				"\r\n" + 
				"      \"bank\": {\r\n" + 
				"\r\n" + 
				"        \"paymentMethodType\": 0,\r\n" + 
				"\r\n" + 
				"        \"orderId\": 0,\r\n" + 
				"\r\n" + 
				"        \"isDefaultPaymentMethod\": false\r\n" + 
				"\r\n" + 
				"      },\r\n" + 
				"\r\n" + 
				"      \"card\": {\r\n" + 
				"\r\n" + 
				"        \"paymentMethodType\": 0,\r\n" + 
				"\r\n" + 
				"        \"orderId\": 0,\r\n" + 
				"\r\n" + 
				"        \"isDefaultPaymentMethod\": false\r\n" + 
				"\r\n" + 
				"      }\r\n" + 
				"\r\n" + 
				"    },\r\n" + 
				"\r\n" + 
				"    \"walletShortCode\": \"1234\",\r\n" + 
				"\r\n" + 
				"    \"transactionEnabled\": false,\r\n" + 
				"\r\n" + 
				"    \"templateProfile\": {},\r\n" + 
				"\r\n" + 
				"    \"superAgent\": false,\r\n" + 
				"\r\n" + 
				"    \"bankInformation\": {\r\n" + 
				"\r\n" + 
				"      \"paymentMethodType\": 0,\r\n" + 
				"\r\n" + 
				"      \"orderId\": 0,\r\n" + 
				"\r\n" + 
				"      \"isDefaultPaymentMethod\": false\r\n" + 
				"\r\n" + 
				"    },\r\n" + 
				"\r\n" + 
				"    \"authenticationType\": 1,\r\n" + 
				"\r\n" + 
				"    \"language\": \"en\",\r\n" + 
				"\r\n" + 
				"    \"userRiskProfile\": {},\r\n" + 
				"\r\n" + 
				"    \"plainPassword\": true,\r\n" + 
				"\r\n" + 
				"    \"hashedPassword\": false,\r\n" + 
				"\r\n" + 
				"    \"loginMethod\": \"normal\"\r\n" + 
				"\r\n" + 
				"  },\r\n" + 
				"\r\n" + 
				"  \"otherSenders\": [],\r\n" + 
				"\r\n" + 
				"  \"primaryReceiverInfo\": {\r\n" + 
				"  	\"msisdn\":\"2327060706071\",\r\n" + 
				"\"countryIso2\":\"NG\",\r\n" + 
				"    \"balance\": 0,\r\n" + 
				"\r\n" + 
				"    \"fee\": 0,\r\n" + 
				"\r\n" + 
				"    \"entityInfo\": {},\r\n" + 
				"\r\n" + 
				"    \"personalDetails\": {},\r\n" + 
				"\r\n" + 
				"    \"paymentMethod\": {\r\n" + 
				"\r\n" + 
				"      \"bank\": {\r\n" + 
				"\r\n" + 
				"        \"paymentMethodType\": 0,\r\n" + 
				"\r\n" + 
				"        \"orderId\": 0,\r\n" + 
				"\r\n" + 
				"        \"isDefaultPaymentMethod\": false\r\n" + 
				"\r\n" + 
				"      },\r\n" + 
				"\r\n" + 
				"      \"card\": {\r\n" + 
				"\r\n" + 
				"        \"paymentMethodType\": 0,\r\n" + 
				"\r\n" + 
				"        \"orderId\": 0,\r\n" + 
				"\r\n" + 
				"        \"isDefaultPaymentMethod\": false\r\n" + 
				"\r\n" + 
				"      }\r\n" + 
				"\r\n" + 
				"    },\r\n" + 
				"\r\n" + 
				"    \"transactionEnabled\": true,\r\n" + 
				"\r\n" + 
				"    \"templateProfile\": {},\r\n" + 
				"\r\n" + 
				"    \"superAgent\": false,\r\n" + 
				"\r\n" + 
				"    \"bankInformation\": {\r\n" + 
				"\r\n" + 
				"      \"paymentMethodType\": 0,\r\n" + 
				"\r\n" + 
				"      \"orderId\": 0,\r\n" + 
				"\r\n" + 
				"      \"isDefaultPaymentMethod\": false\r\n" + 
				"\r\n" + 
				"    },\r\n" + 
				"\r\n" + 
				"    \"userRiskProfile\": {},\r\n" + 
				"\r\n" + 
				"    \"plainPassword\": false,\r\n" + 
				"\r\n" + 
				"    \"hashedPassword\": false\r\n" + 
				"\r\n" + 
				"  },\r\n" + 
				"\r\n" + 
				"  \"otherReceivers\": [],\r\n" + 
				"\r\n" + 
				"  \"transactionInfo\": {\r\n" + 
				"\r\n" + 
				"    \"transactionAmount\": 1018.11,\r\n" + 
				"\r\n" + 
				"    \"voucher\": {},\r\n" + 
				"\r\n" + 
				"    \"transactionDefinitionId\": 16143023,\r\n" + 
				"\r\n" + 
				"    \"transactionId\": 16146194,\r\n" + 
				"\r\n" + 
				"    \"transactionFeeAmount\": 0,\r\n" + 
				"\r\n" + 
				"    \"transactionVatAmount\": 0\r\n" + 
				"\r\n" + 
				"  },\r\n" + 
				"\r\n" + 
				"  \"responseTextMessage\": \"\",\r\n" + 
				"\r\n" + 
				"  \"notification\": {\r\n" + 
				"\r\n" + 
				"    \"enabled\": false,\r\n" + 
				"\r\n" + 
				"    \"smsMessages\": []\r\n" + 
				"\r\n" + 
				"  },\r\n" + 
				"\r\n" + 
				"  \"status\": {\r\n" + 
				"\r\n" + 
				"    \"errorFlag\": false,\r\n" + 
				"	\"statusCode\": \"\",\r\n" + 
				"    \"statusHints\": []\r\n" + 
				"\r\n" + 
				"  },\r\n" + 
				"\"iTopup\":{\r\n" + 
				"	\"topupProduct\":{\r\n" + 
				"		\"product_id\": \"224\"\r\n" + 
				"	},\r\n" + 
				"	\"operator_id\":\"1\",\r\n" + 
				"	\"msisdnInquiryResult\":{\r\n" + 
				"		\"opts\":{\r\n" + 
				"			\"hasOpenRange\":\"\",\r\n" + 
				"			\"country\":\"\",\r\n" + 
				"			\"iso\":\"\",\r\n" + 
				"			\"canOverride\":\"\",\r\n" + 
				"			\"msisdn\":\"\"\r\n" + 
				"		},\r\n" + 
				"		\"operatorsList\":[]\r\n" + 
				"	}\r\n" + 
				"},\r\n" + 
				"  \"workflowInfo\": {\r\n" + 
				"\r\n" + 
				"    \"needApproval\": false,\r\n" + 
				"\r\n" + 
				"    \"pauseStep\": 1,\r\n" + 
				"\r\n" + 
				"    \"isResume\": false\r\n" + 
				"\r\n" + 
				"  },\r\n" + 
				"\r\n" + 
				"  \"biller\": {},\r\n" + 
				"\r\n" + 
				"  \"softFields\": {\r\n" + 
				"\r\n" + 
				"    \"DELETE_SENSITIVE_DATA_SERVICES\": \"4250,4254,4253,4301,4302\",\r\n" + 
				"\r\n" + 
				"    \"AddedKyc\": \"true\",\r\n" + 
				"\r\n" + 
				"    \"serviceMode\": \"Normal\",\r\n" + 
				"\r\n" + 
				"    \"SERVICE_LOG\": {\r\n" + 
				"\r\n" + 
				"      \"id\": 16146193,\r\n" + 
				"\r\n" + 
				"      \"requestId\": \"26ffc379f67211e78fabf7b632e28ee4\",\r\n" + 
				"\r\n" + 
				"      \"serviceId\": \"16139776\",\r\n" + 
				"\r\n" + 
				"      \"serviceCode\": \"45011\",\r\n" + 
				"\r\n" + 
				"      \"sender\": {\r\n" + 
				"\r\n" + 
				"        \"userKey\": \"1234+2348071236545\",\r\n" + 
				"\r\n" + 
				"        \"msisdn\": \"+2348071236545\",\r\n" + 
				"\r\n" + 
				"        \"walletShortCode\": \"1234\"\r\n" + 
				"\r\n" + 
				"      },\r\n" + 
				"\r\n" + 
				"      \"receiver\": {},\r\n" + 
				"\r\n" + 
				"      \"creationDate\": \"Jan 11, 2018 3:53:01 AM\",\r\n" + 
				"\r\n" + 
				"      \"startDate\": \"Jan 11, 2018 3:53:01 AM\",\r\n" + 
				"\r\n" + 
				"      \"lastModifiedDate\": \"Jan 11, 2018 3:53:05 AM\",\r\n" + 
				"\r\n" + 
				"      \"status\": \"Initiated\",\r\n" + 
				"\r\n" + 
				"      \"walletShortCode\": \"1234\",\r\n" + 
				"\r\n" + 
				"      \"steps\": [],\r\n" + 
				"\r\n" + 
				"      \"exposed\": false,\r\n" + 
				"\r\n" + 
				"      \"defaultService\": false,\r\n" + 
				"\r\n" + 
				"      \"amount\": 1018.11\r\n" + 
				"\r\n" + 
				"    },\r\n" + 
				"\r\n" + 
				"    \"CURRENT_STEP\": 16139777,\r\n" + 
				"\r\n" + 
				"    \"currentLogStepId\": 16145029,\r\n" + 
				"\r\n" + 
				"    \"ENABLE_LOGGING_REQUEST_RESPONSE\": true,\r\n" + 
				"\r\n" + 
				"    \"END_TO_END_REQUEST\": {\r\n" + 
				"\r\n" + 
				"      \"id\": 15382769,\r\n" + 
				"\r\n" + 
				"      \"serviceType\": \"45011\",\r\n" + 
				"\r\n" + 
				"      \"msisdn\": \"+2348071236545\",\r\n" + 
				"\r\n" + 
				"      \"startDate\": \"Jan 11, 2018 3:52:59 AM\",\r\n" + 
				"\r\n" + 
				"      \"endDate\": \"Jan 11, 2018 3:53:00 AM\",\r\n" + 
				"\r\n" + 
				"      \"amount\": 1018.11,\r\n" + 
				"\r\n" + 
				"      \"status\": \"Not_Completed\",\r\n" + 
				"\r\n" + 
				"      \"leg\": \"legDev\"\r\n" + 
				"\r\n" + 
				"    }\r\n" + 
				"\r\n" + 
				"  },\r\n" + 
				"\r\n" + 
				"  \"externalInterfaceResponse\": {\r\n" + 
				"\r\n" + 
				"    \"status\": \"RESPONSE_TIME_OUT\"\r\n" + 
				"\r\n" + 
				"  },\r\n" + 
				"\r\n" + 
				"  \"messageSequeanceNumber\": 506,\r\n" + 
				"\r\n" + 
				"  \"externalTransactionInformations\": [],\r\n" + 
				"\r\n" + 
				"  \"chequeBook\": {},\r\n" + 
				"\r\n" + 
				"  \"clientInfo\": {\r\n" + 
				"\r\n" + 
				"    \"packageName\": \"UBA_Cross_App\",\r\n" + 
				"\r\n" + 
				"    \"platform\": \"Android 4.4.4;Huawei LON-AL00\",\r\n" + 
				"\r\n" + 
				"    \"mode\": \"Debug\",\r\n" + 
				"\r\n" + 
				"    \"versionName\": \"5.1.2\",\r\n" + 
				"\r\n" + 
				"    \"versionCode\": \"501020\",\r\n" + 
				"\r\n" + 
				"    \"version\": {\r\n" + 
				"\r\n" + 
				"      \"major\": \"5\",\r\n" + 
				"\r\n" + 
				"      \"minor\": \"1\",\r\n" + 
				"\r\n" + 
				"      \"build\": \"2\"\r\n" + 
				"\r\n" + 
				"    },\r\n" + 
				"\r\n" + 
				"    \"deviceInfo\": {\r\n" + 
				"\r\n" + 
				"      \"carrierName\": \"EMS - Mobinil\",\r\n" + 
				"\r\n" + 
				"      \"countryCode\": \"eg\",\r\n" + 
				"\r\n" + 
				"      \"imei\": \"200556293813621\",\r\n" + 
				"\r\n" + 
				"      \"networkType\": \"3\",\r\n" + 
				"\r\n" + 
				"      \"manufacturer\": \"Huawei\",\r\n" + 
				"\r\n" + 
				"      \"model\": \"LON-AL00\",\r\n" + 
				"\r\n" + 
				"      \"serialNumber\": \"93815461\"\r\n" + 
				"\r\n" + 
				"    }\r\n" + 
				"\r\n" + 
				"  },\r\n" + 
				"\r\n" + 
				"  \"userRiskProfiles\": [],\r\n" + 
				"\r\n" + 
				"  \"indemnityProfile\": {\r\n" + 
				"\r\n" + 
				"    \"isIndemnityProfileEnabled\": 0\r\n" + 
				"\r\n" + 
				"  },\r\n" + 
				"\r\n" + 
				"  \"serviceSuccessResponse\": {},\r\n" + 
				"\r\n" + 
				"  \"walletInfo\": {\r\n" + 
				"\r\n" + 
				"    \"smsSenderId\": \"Vericash\",\r\n" + 
				"\r\n" + 
				"    \"userGroup\": \"Nigeria\",\r\n" + 
				"\r\n" + 
				"    \"currency\": \"NGN\",\r\n" + 
				"\r\n" + 
				"    \"language\": \"en\",\r\n" + 
				"\r\n" + 
				"    \"countryIso2\": \"NG\",\r\n" + 
				"\r\n" + 
				"    \"countryIso3\": \"NGA\",\r\n" + 
				"\r\n" + 
				"    \"countryCode\": \"234\",\r\n" + 
				"\r\n" + 
				"    \"walletName\": \"UMOBNIG\",\r\n" + 
				"\r\n" + 
				"    \"walletShortCode\": \"1234\",\r\n" + 
				"\r\n" + 
				"    \"countryName\": \"NIGERIA\",\r\n" + 
				"\r\n" + 
				"    \"timeZone\": \"GMT+1\",\r\n" + 
				"\r\n" + 
				"    \"balanceDecimalPoints\": 0,\r\n" + 
				"\r\n" + 
				"    \"balanceDivideFactor\": 100,\r\n" + 
				"\r\n" + 
				"    \"minTransferAmount\": \"1\",\r\n" + 
				"\r\n" + 
				"    \"currencyShortCode\": \"N\"\r\n" + 
				"\r\n" + 
				"  },\r\n" + 
				"   \"extraFields\": {\r\n" + 
				"    	\"transactionInfo\":{\r\n" + 
				"    		\"transactionId\":\"838824238\"\r\n" + 
				"    	},\r\n" + 
				"    	\"externalTransactionPeriod\":{\r\n" + 
				"    		\"startDate\":\"29May2018\",\r\n" + 
				"    		\"endDate\":\"29May2018\"\r\n" + 
				"    	},\r\n" + 
				"    	\"GetTrx\":{\r\n" + 
				"    		\"SessionID\":\"2C5C2967-A565-4BE1-B860-39A17BA42C21\",\r\n" + 
				"    		\"TrxDate\":\"29May2018\"\r\n" + 
				"    	},\r\n" + 
				"    	\"GetReport\":{\r\n" + 
				"    		\"SessionID\":\"2C5C2967-A565-4BE1-B860-39A17BA42C21\"\r\n" + 
				"    	},\r\n" + 
				"    	\"GetAllListOfPartners\":{\r\n" + 
				"    		\"SessionID\":\"2C5C2967-A565-4BE1-B860-39A17BA42C21\"\r\n" + 
				"    	},\r\n" + 
				"    	\"getBank\":{\r\n" + 
				"    		\"blz\":\"hello\"\r\n" + 
				"    	},\r\n" + 
				"    	\"search\":{\r\n" + 
				"    		\"request\":{\r\n" + 
				"    			\"app_id\":\"2452014150\",\r\n" + 
				"    			\"app_key\":\"test\",\r\n" + 
				"    			\"search_text\":\"0000013646\",\r\n" + 
				"    			\"wallet_currency\":\"GHS\"\r\n" + 
				"    		}\r\n" + 
				"    	}\r\n" + 
				"    	\r\n" + 
				"    }\r\n" + 
				"\r\n" + 
				"}";
		TextMessage textMessage = null;
		try {
			textMessage = jmsTemplate.getConnectionFactory().createConnection()
					.createSession(false, Session.AUTO_ACKNOWLEDGE).createTextMessage();
			textMessage.setText(request);
			textMessage.setJMSCorrelationID("1234");
			textMessage.setStringProperty(Headers.SERVICE_ID.name(), "elma-GetTrx");
			textMessage.setStringProperty(Headers.logId.name(), "12");

			textMessage.setStringProperty(Headers.stackId.name(), "13");
			//textMessage.setIntProperty(Headers.STEP_ORDER.name(), messageHeader.getStepOrder());
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		jmsTemplate.convertAndSend("vc-integration-bm-req", textMessage);
	}
}
