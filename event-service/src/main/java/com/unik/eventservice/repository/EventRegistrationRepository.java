package com.unik.eventservice.repository;

import com.unik.eventservice.entity.EventRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EventRegistrationRepository extends JpaRepository<EventRegistration, UUID> {
    Optional<EventRegistration> findByEventIdAndUserId(UUID eventId, UUID userId);
}
