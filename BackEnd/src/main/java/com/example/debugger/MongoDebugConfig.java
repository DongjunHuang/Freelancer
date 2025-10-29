package com.example.debugger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.mongodb.autoconfigure.MongoProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class MongoDebugConfig {
  private static final Logger log = LoggerFactory.getLogger(MongoDebugConfig.class);

  @Bean
  CommandLineRunner showMongoProps(MongoProperties props) {
    return args -> {
      log.info("[MONGO] uri={}", props.getUri());
      log.info("[MONGO] host={}, port={}, db={}, authDb={}, user={}",
          props.getHost(), props.getPort(), props.getDatabase(),
          props.getAuthenticationDatabase(), props.getUsername());
    };
  }
}