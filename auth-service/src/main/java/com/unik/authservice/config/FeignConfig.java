package com.unik.authservice.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    private static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";

    @Bean
    RequestInterceptor internalTokenRequestInterceptor(AppProperties appProperties) {
        return template -> template.header(INTERNAL_TOKEN_HEADER, appProperties.internal().token());
    }
}
