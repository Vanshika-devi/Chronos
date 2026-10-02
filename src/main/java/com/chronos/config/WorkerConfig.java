package com.chronos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class WorkerConfig{
    @Bean
    public ExecutorService jobExecutor(){
        return Executors.newFixedThreadPool(4);
    }
}