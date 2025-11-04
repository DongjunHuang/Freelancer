package com.example.functiontest;

import java.util.Optional;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BootLogger implements CommandLineRunner {
    private final org.springframework.core.env.Environment env;
    
    @Override 
    public void run(String... args) {
        log.info("spring.mail.host={}", env.getProperty("spring.mail.host"));
        log.info("spring.mail.port={}", env.getProperty("spring.mail.port"));
        log.info("spring.mail.username={}", env.getProperty("spring.mail.username"));
        log.info("profiles={}", String.join(",", env.getActiveProfiles()));
        log.info("mail.pw.len={}", Optional.ofNullable(env.getProperty("spring.mail.password")).map(String::length).orElse(0));
    }
}