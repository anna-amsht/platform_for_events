package com.unik.userservice.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String company,
        String jobTitle,
        String city,
        String avatarUrl,
        String bio,
        List<InterestResponse> interests,
        Instant createdAt,
        Instant updatedAt
) {
}
