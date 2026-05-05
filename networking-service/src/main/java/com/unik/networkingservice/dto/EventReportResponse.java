package com.unik.networkingservice.dto;

import java.time.Instant;
import java.util.UUID;

public record EventReportResponse(
        UUID eventId,
        UUID authorId,
        String fileName,
        String contentType,
        String contentData,
        Instant createdAt,
        Instant updatedAt
) {}
