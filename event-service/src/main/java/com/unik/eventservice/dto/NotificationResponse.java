package com.unik.eventservice.dto;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        String type,
        String title,
        String message,
        UUID eventId,
        boolean read,
        Instant readAt,
        Instant createdAt
) {
}
