package com.unik.eventservice.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record InviteUsersRequest(@NotEmpty List<UUID> userIds) {}
