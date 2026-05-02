package com.unik.authservice.dto.response;

import java.util.UUID;

public record UserMeResponse(UUID userId, String email, String role) {
}
