package com.cit.vc;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jms.annotation.EnableJms;

import com.cit.vc.jms.JmsPublisher;
import com.cit.vc.service.ListenOnFilesProperties;

//@EnableDiscoveryClient
@EnableJms
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
// @EnableRedisRepositories(basePackages="com.cit.vc.repository")
@ComponentScan(basePackages = "com.cit.vc")
@EnableHystrixDashboard
@EnableCircuitBreaker
public class VcIntegrationApplication implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger("filelogger");

	@Value("${path.File}")
	private String path;

	@Autowired
	private JmsPublisher jmsPublisher;
	
	public static void main(String[] args) {
		SpringApplication.run(VcIntegrationApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		try {
			listenOnFilesProperties().processEvents();
			//jmsPublisher.sendRequest();
		} catch (IOException e) {
			logger.error("there is an error in ListnerOnFiles");
		}
	}

	@Bean
	public ListenOnFilesProperties listenOnFilesProperties() throws IOException {
		Path dir = Paths.get(path);
		return new ListenOnFilesProperties(dir);
	}
}
