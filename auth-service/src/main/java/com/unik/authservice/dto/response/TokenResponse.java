package com.unik.authservice.dto.response;

public record TokenResponse(String accessToken, String tokenType, long expiresInSeconds, String role) {
}
