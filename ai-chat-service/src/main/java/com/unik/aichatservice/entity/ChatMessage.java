package com.unik.aichatservice.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "ai", name = "chat_messages")
public class ChatMessage {
    @Id
    private UUID id;
    @Column(name = "session_id", nullable = false)
    private UUID sessionId;
    @Column(nullable = false, length = 32)
    private String role;
    @Column(nullable = false, length = 4000)
    private String content;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected ChatMessage() {}
    public static ChatMessage create(UUID sessionId, String role, String content) {
        ChatMessage message = new ChatMessage();
        message.id = UUID.randomUUID();
        message.sessionId = sessionId;
        message.role = role;
        message.content = content;
        return message;
    }
    @PrePersist
    void onCreate() { createdAt = Instant.now(); }
    public UUID getId() { return id; }
    public UUID getSessionId() { return sessionId; }
    public String getRole() { return role; }
    public String getContent() { return content; }
    public Instant getCreatedAt() { return createdAt; }
}
