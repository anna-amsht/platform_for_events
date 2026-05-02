package com.unik.aichatservice;

import com.unik.aichatservice.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@EnableConfigurationProperties(AppProperties.class)
public class AiChatServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiChatServiceApplication.class, args);
    }
}
