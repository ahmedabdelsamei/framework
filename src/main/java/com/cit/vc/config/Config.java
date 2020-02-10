package com.cit.vc.config;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.reactor.IOReactorException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.http.client.HttpComponentsAsyncClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;


@Configuration
public class Config {

	 @Value("${spring.activemq.broker-url}")
	 private String brokerUrl;

	 @Value("${spring.activemq.user}")
	 private String userName;
	
	 @Value("${spring.activemq.password}")
	 private String password;
	 

	@Bean
	public PoolingHttpClientConnectionManager poolingHttpClientConnectionManager() {

		PoolingHttpClientConnectionManager result = new PoolingHttpClientConnectionManager();
		result.setDefaultMaxPerRoute(20000);
		result.setMaxTotal(50000);
		return result;
	}

	@Bean
	public PoolingNHttpClientConnectionManager poolingNHttpClientConnectionManager() {
		PoolingNHttpClientConnectionManager result = null;
		try {
			result = new PoolingNHttpClientConnectionManager(new DefaultConnectingIOReactor(IOReactorConfig.DEFAULT));
		} catch (IOReactorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		result.setDefaultMaxPerRoute(20000);
		result.setMaxTotal(50000);
		return result;
	}

	@Bean
	public RequestConfig requestConfig() {

		RequestConfig result = RequestConfig.custom()
				.setConnectionRequestTimeout(60000)
				.setConnectTimeout(60000)
				.setSocketTimeout(60000).build();
		return result;
	}

	@Bean
	public CloseableHttpClient httpClient(PoolingHttpClientConnectionManager poolingHttpClientConnectionManager,
			RequestConfig requestConfig) {

		CloseableHttpClient result = HttpClientBuilder.create()
				.setConnectionManager(poolingHttpClientConnectionManager)
				.setDefaultRequestConfig(requestConfig).build();
		return result;
	}

	@Bean
	public CloseableHttpAsyncClient asyncHttpClient(
		PoolingNHttpClientConnectionManager poolingNHttpClientConnectionManager, RequestConfig requestConfig) {
		
		CloseableHttpAsyncClient result = HttpAsyncClientBuilder.create()
				.setConnectionManager(poolingNHttpClientConnectionManager)
				.setDefaultRequestConfig(requestConfig)
				.build();
		return result;
	}

	// @LoadBalanced
	@Bean
	public RestTemplate restTemplate(HttpClient httpClient) {

		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		requestFactory.setHttpClient(httpClient);

		return new RestTemplate(requestFactory);
	}

	/*@Bean
	JedisConnectionFactory jedisConnectionFactory() {
		JedisConnectionFactory connectionFactory = new JedisConnectionFactory();
		// connectionFactory.setHostName("docker");
		connectionFactory.setHostName("redis");
		connectionFactory.setPort(6379);
		return connectionFactory;
	}*/

	/*@Bean
	public RedisTemplate<String, Message> redisTemplate() {
		RedisTemplate<String, Message> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(jedisConnectionFactory());
		redisTemplate.setDefaultSerializer(new StringRedisSerializer());
		return redisTemplate;
	}*/


	@Bean
	public AsyncRestTemplate asyncRestTemplate(HttpAsyncClient httpAsyncClient) {

		HttpComponentsAsyncClientHttpRequestFactory result = new HttpComponentsAsyncClientHttpRequestFactory();
		result.setAsyncClient(httpAsyncClient);
		return new AsyncRestTemplate(result);
	}
	
	
	@Bean
	public DefaultJmsListenerContainerFactory defaultContainerFactory() {
		DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
//		factory.setReceiveTimeout(4000l);
		factory.setConcurrency("50-100");
		factory.setConnectionFactory(connectionFactory());

		return factory;
	}

	@Bean
	public ConnectionFactory connectionFactory() {
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
		connectionFactory.setUseAsyncSend(true);
		connectionFactory.setBrokerURL(brokerUrl);
		connectionFactory.setUserName(userName);
		connectionFactory.setPassword(password);
		return connectionFactory;
	}

}