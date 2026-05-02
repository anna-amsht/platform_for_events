package com.unik.aichatservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Jwt jwt, Ai ai) {
    public record Jwt(String secret) {}
    public record Ai(boolean enabled) {}
}
