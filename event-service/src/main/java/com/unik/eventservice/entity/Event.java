package com.unik.eventservice.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "events", name = "events")
public class Event {
    @Id
    private UUID id;
    @Column(name = "organizer_id", nullable = false)
    private UUID organizerId;
    @Column(nullable = false, length = 200)
    private String title;
    @Column(length = 2000)
    private String description;
    private String location;
    private Double latitude;
    private Double longitude;
    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;
    @Column(name = "ends_at", nullable = false)
    private Instant endsAt;
    @Column(nullable = false)
    private Integer capacity;
    @Column(name = "available_seats", nullable = false)
    private Integer availableSeats;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    protected Event() {}

    public static Event create(UUID organizerId, String title, String description, String location, Double latitude,
                               Double longitude, Instant startsAt, Instant endsAt, Integer capacity) {
        Event event = new Event();
        event.id = UUID.randomUUID();
        event.organizerId = organizerId;
        event.update(title, description, location, latitude, longitude, startsAt, endsAt, capacity, 0L);
        return event;
    }

    public void update(String title, String description, String location, Double latitude, Double longitude,
                       Instant startsAt, Instant endsAt, Integer capacity, long registeredParticipants) {
        if (capacity < registeredParticipants) {
            throw new IllegalArgumentException("Event capacity cannot be less than current registrations count");
        }
        this.title = title.trim();
        this.description = normalize(description);
        this.location = normalize(location);
        this.latitude = latitude;
        this.longitude = longitude;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.capacity = capacity;
        this.availableSeats = capacity - Math.toIntExact(registeredParticipants);
    }

    public void reserveSeat() {
        if (availableSeats <= 0) {
            throw new IllegalArgumentException("No available seats for event");
        }
        availableSeats--;
    }

    public void releaseSeat() {
        if (availableSeats < capacity) {
            availableSeats++;
        }
    }

    public void cancel() {
        this.cancelledAt = Instant.now();
    }

    public boolean isCancelled() {
        return cancelledAt != null;
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

    private static String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public UUID getId() { return id; }
    public UUID getOrganizerId() { return organizerId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public Instant getStartsAt() { return startsAt; }
    public Instant getEndsAt() { return endsAt; }
    public Integer getCapacity() { return capacity; }
    public Integer getAvailableSeats() { return availableSeats; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getCancelledAt() { return cancelledAt; }
}
