package com.unik.eventservice.repository;

import com.unik.eventservice.entity.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvitationRepository extends JpaRepository<Invitation, UUID> {
    List<Invitation> findAllByEventId(UUID eventId);
    Optional<Invitation> findByEventIdAndInviteeUserId(UUID eventId, UUID inviteeUserId);
    boolean existsByEventIdAndInviteeUserId(UUID eventId, UUID inviteeUserId);
}
