package com.unik.authservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Jwt jwt, Frontend frontend, Oauth2 oauth2, Github github, Internal internal) {

    public record Jwt(String secret, long expirationMs) {
    }

    public record Frontend(String url) {
    }

    public record Oauth2(String callbackBaseUrl) {
    }

    public record Github(String clientId, String clientSecret) {
    }

    public record Internal(String token) {
    }
}
