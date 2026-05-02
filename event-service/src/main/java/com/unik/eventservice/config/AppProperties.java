package com.unik.eventservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Jwt jwt, Weather weather) {
    public record Jwt(String secret) {}
    public record Weather(String apiKey) {}
}
