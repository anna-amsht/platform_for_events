package com.unik.userservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Jwt jwt, Internal internal) {

    public record Jwt(String secret) {
    }

    public record Internal(String token) {
    }
}
