package com.unik.networkingservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record EventReportRequest(
        @NotNull UUID authorId,
        @NotBlank @Size(max = 255) String fileName,
        @NotBlank @Size(max = 120) String contentType,
        @NotBlank String contentData
) {}
