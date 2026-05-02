package com.unik.eventservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CreateEventRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 2000) String description,
        @Size(max = 255) String location,
        Double latitude,
        Double longitude,
        @NotNull Instant startsAt,
        @NotNull Instant endsAt
) {}
