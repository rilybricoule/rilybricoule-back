package com.sbsolutions.rilybricoule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.sbsolutions.rilybricoule")
@EntityScan(basePackages = "com.sbsolutions.rilybricoule.entity")
@EnableScheduling
public class RilybricouleApplication {

	public static void main(String[] args) {
		SpringApplication.run(RilybricouleApplication.class, args);
	}

}
