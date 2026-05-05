package com.unik.eventservice.repository;

import com.unik.eventservice.entity.EventRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventRegistrationRepository extends JpaRepository<EventRegistration, UUID> {
    Optional<EventRegistration> findByEventIdAndUserId(UUID eventId, UUID userId);
    List<EventRegistration> findAllByEventId(UUID eventId);
    List<EventRegistration> findAllByUserId(UUID userId);
    long countByEventId(UUID eventId);
    void deleteByEventIdAndUserId(UUID eventId, UUID userId);
    void deleteAllByEventId(UUID eventId);
}
