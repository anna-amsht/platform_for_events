package com.unik.networkingservice.dto;

import java.time.Instant;
import java.util.UUID;

public record ChatMessageResponse(UUID id, UUID eventId, String roleName, UUID senderId, String content, Instant createdAt) {}
