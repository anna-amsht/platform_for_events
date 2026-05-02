package com.unik.eventservice.repository;

import com.unik.eventservice.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findAllByRecipientUserIdOrderByCreatedAtDesc(UUID recipientUserId);
    Optional<Notification> findByIdAndRecipientUserId(UUID id, UUID recipientUserId);
}
