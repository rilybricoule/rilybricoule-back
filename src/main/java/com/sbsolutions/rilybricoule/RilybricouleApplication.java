package com.sbsolutions.rilybricoule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(scanBasePackages = "com.sbsolutions.rilybricoule")
@EntityScan(basePackages = "com.sbsolutions.rilybricoule.entity")
public class RilybricouleApplication {

	public static void main(String[] args) {
		SpringApplication.run(RilybricouleApplication.class, args);
	}

}
