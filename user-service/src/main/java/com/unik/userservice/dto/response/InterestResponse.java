package com.unik.userservice.dto.response;

import java.util.UUID;

public record InterestResponse(UUID id, String slug, String name) {
}
