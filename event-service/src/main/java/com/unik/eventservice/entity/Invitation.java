package com.unik.eventservice.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "events", name = "invitations")
public class Invitation {
    @Id
    private UUID id;
    @Column(name = "event_id", nullable = false)
    private UUID eventId;
    @Column(name = "invitee_user_id", nullable = false)
    private UUID inviteeUserId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvitationStatus status;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Invitation() {}
    public static Invitation create(UUID eventId, UUID inviteeUserId) {
        Invitation invitation = new Invitation();
        invitation.id = UUID.randomUUID();
        invitation.eventId = eventId;
        invitation.inviteeUserId = inviteeUserId;
        invitation.status = InvitationStatus.PENDING;
        return invitation;
    }
    @PrePersist
    void onCreate() { createdAt = Instant.now(); }
    public void accept() { status = InvitationStatus.ACCEPTED; }
    public void decline() { status = InvitationStatus.DECLINED; }
    public UUID getId() { return id; }
    public UUID getEventId() { return eventId; }
    public UUID getInviteeUserId() { return inviteeUserId; }
    public InvitationStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
