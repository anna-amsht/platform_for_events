package com.unik.eventservice.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "events", name = "registrations")
public class EventRegistration {
    @Id
    private UUID id;
    @Column(name = "event_id", nullable = false)
    private UUID eventId;
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    @Column(name = "role_name", nullable = false)
    private String roleName;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected EventRegistration() {}
    public static EventRegistration create(UUID eventId, UUID userId, String roleName) {
        EventRegistration registration = new EventRegistration();
        registration.id = UUID.randomUUID();
        registration.eventId = eventId;
        registration.userId = userId;
        registration.roleName = roleName;
        return registration;
    }
    @PrePersist
    void onCreate() { createdAt = Instant.now(); }
    public UUID getId() { return id; }
    public UUID getEventId() { return eventId; }
    public UUID getUserId() { return userId; }
    public String getRoleName() { return roleName; }
    public Instant getCreatedAt() { return createdAt; }
}
