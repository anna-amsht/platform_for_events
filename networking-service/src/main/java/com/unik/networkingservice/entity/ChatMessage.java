package com.unik.networkingservice.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "networking", name = "chat_messages")
public class ChatMessage {
    @Id
    private UUID id;
    @Column(name = "event_id", nullable = false)
    private UUID eventId;
    @Column(name = "role_name", nullable = false)
    private String roleName;
    @Column(name = "sender_id", nullable = false)
    private UUID senderId;
    @Column(nullable = false, length = 2000)
    private String content;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected ChatMessage() {}
    public static ChatMessage create(UUID eventId, String roleName, UUID senderId, String content) {
        ChatMessage message = new ChatMessage();
        message.id = UUID.randomUUID();
        message.eventId = eventId;
        message.roleName = roleName.trim().toUpperCase();
        message.senderId = senderId;
        message.content = content.trim();
        return message;
    }
    @PrePersist
    void onCreate() { createdAt = Instant.now(); }
    public UUID getId() { return id; }
    public UUID getEventId() { return eventId; }
    public String getRoleName() { return roleName; }
    public UUID getSenderId() { return senderId; }
    public String getContent() { return content; }
    public Instant getCreatedAt() { return createdAt; }
}
