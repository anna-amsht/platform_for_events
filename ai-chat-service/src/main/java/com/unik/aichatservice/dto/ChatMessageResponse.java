package com.unik.aichatservice.dto;

import java.time.Instant;
import java.util.UUID;

public record ChatMessageResponse(UUID id, UUID sessionId, String role, String content, Instant createdAt) {}
