package com.unik.authservice.dto.internal;

import java.util.UUID;

public record UpsertUserProfileRequest(
        UUID userId,
        String email
) {
}
