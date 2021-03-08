package com.atg.atgtools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@SpringBootApplication
@EnableAsync
public class AtgtoolsApplication {
	Logger logger = LoggerFactory.getLogger(AtgtoolsApplication.class);

	public static void main(String[] args) {

		SpringApplication.run(AtgtoolsApplication.class, args);
	}



}
