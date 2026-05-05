package com.unik.userservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpsertUserProfileRequest(
        @NotNull UUID userId,
        @Email @Size(max = 320) String email
) {
}
