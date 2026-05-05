package com.unik.eventservice.dto;

import java.time.Instant;
import java.util.UUID;

public record InvitationResponse(UUID id, UUID eventId, UUID inviteeUserId, String status, Instant createdAt) {}
