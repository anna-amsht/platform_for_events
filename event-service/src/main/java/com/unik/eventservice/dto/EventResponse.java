package com.unik.eventservice.dto;

import java.time.Instant;
import java.util.UUID;

public record EventResponse(UUID id, UUID organizerId, String title, String description, String location,
                            Double latitude, Double longitude, Instant startsAt, Instant endsAt,
                            Instant createdAt, Instant updatedAt) {}
