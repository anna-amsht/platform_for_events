package com.unik.networkingservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "networking", name = "event_reports")
public class EventReport {
    @Id
    @Column(name = "event_id", nullable = false)
    private UUID eventId;
    @Column(name = "author_id", nullable = false)
    private UUID authorId;
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;
    @Column(name = "content_type", nullable = false, length = 120)
    private String contentType;
    @Column(name = "content_data", nullable = false, columnDefinition = "text")
    private String contentData;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected EventReport() {
    }

    public static EventReport create(UUID eventId, UUID authorId, String fileName, String contentType, String contentData) {
        EventReport report = new EventReport();
        report.eventId = eventId;
        report.update(authorId, fileName, contentType, contentData);
        return report;
    }

    public void update(UUID authorId, String fileName, String contentType, String contentData) {
        this.authorId = authorId;
        this.fileName = fileName.trim();
        this.contentType = contentType.trim();
        this.contentData = contentData.trim();
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getEventId() { return eventId; }
    public UUID getAuthorId() { return authorId; }
    public String getFileName() { return fileName; }
    public String getContentType() { return contentType; }
    public String getContentData() { return contentData; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
