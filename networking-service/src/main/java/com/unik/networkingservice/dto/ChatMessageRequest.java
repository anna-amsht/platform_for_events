package com.unik.networkingservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ChatMessageRequest(@NotNull UUID senderId, @NotBlank @Size(max = 2000) String content) {}
