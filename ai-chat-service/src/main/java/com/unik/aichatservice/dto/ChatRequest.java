package com.unik.aichatservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ChatRequest(UUID sessionId, @NotBlank @Size(max = 4000) String message) {}
