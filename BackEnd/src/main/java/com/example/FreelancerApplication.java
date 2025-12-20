package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class FreelancerApplication {

	public static void main(String[] args) {
		SpringApplication.run(FreelancerApplication.class, args);
	}
}
