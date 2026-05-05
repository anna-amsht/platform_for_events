package com.unik.eventservice.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateRegistrationRoleRequest(@NotBlank String roleName) {
}
