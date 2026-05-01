package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FreelancerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FreelancerApplication.class, args);
    }
}
