package com.sbsolutions.rilybricoule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = "com.sbsolutions.rilybricoule")
@EntityScan(basePackages = {"com.sbsolutions.rilybricoule.entity", "com.sbsolutions.rilybricoule.security.domain.model"})
@EnableAsync
public class RilybricouleApplication {

	public static void main(String[] args) {
		SpringApplication.run(RilybricouleApplication.class, args);
	}

}
