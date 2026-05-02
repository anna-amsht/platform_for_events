package com.unik.userservice.dto.request;

import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateMyProfileRequest(
        @Size(max = 100) String firstName,
        @Size(max = 100) String lastName,
        @Size(max = 160) String company,
        @Size(max = 160) String jobTitle,
        @Size(max = 120) String city,
        @Size(max = 500) String avatarUrl,
        @Size(max = 1000) String bio,
        List<@Size(min = 1, max = 80) String> interests
) {
}
