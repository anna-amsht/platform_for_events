package com.unik.eventservice.dto;

import java.time.Instant;
import java.util.UUID;

public record RegistrationResponse(UUID id, UUID eventId, UUID userId, String roleName, Instant createdAt) {}
