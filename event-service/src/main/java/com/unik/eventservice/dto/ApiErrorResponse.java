package com.unik.eventservice.dto;

import java.time.Instant;

public record ApiErrorResponse(String message, Instant timestamp) {
}
