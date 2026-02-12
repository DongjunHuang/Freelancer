package com.example.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/*
 * @Configuration
 * 
 * @EnableJpaRepositories(basePackages = {
 * "com.example.auth.infra.jpa",
 * "com.example.feedback.infra.jpa",
 * "com.example.functiontest.infra.jpa"
 * })
 * 
 * @EntityScan(basePackages = {
 * "com.example.auth.domain",
 * "com.example.feedback.domain",
 * "com.example.functiontest.domain"
 * })
 * public class JpaConfig {
 * }
 */