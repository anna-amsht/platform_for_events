package com.unik.userservice.dto.response;

import java.util.List;
import java.util.UUID;

public record UserSummaryResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String company,
        String jobTitle,
        String city,
        List<InterestResponse> interests
) {
}
