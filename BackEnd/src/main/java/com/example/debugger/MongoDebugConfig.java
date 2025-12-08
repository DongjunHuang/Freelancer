package com.example.debugger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The debugger components used to debug the mongo db connection.
 */
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