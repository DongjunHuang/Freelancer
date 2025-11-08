package com.example.multithreading;

import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class UploadExecutor {

    @Bean("importExecutor")
    public ThreadPoolTaskExecutor importExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(4);
        ex.setMaxPoolSize(Math.max(4, Runtime.getRuntime().availableProcessors()));
        ex.setQueueCapacity(50);               
        ex.setThreadNamePrefix("csv-import-");
        ex.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy()); 
        ex.setWaitForTasksToCompleteOnShutdown(true);
        ex.setAwaitTerminationSeconds(30);
        ex.initialize();
        return ex;
    }
}
