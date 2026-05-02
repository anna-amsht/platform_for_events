package com.unik.eventservice.service;

import com.unik.eventservice.dto.NotificationResponse;
import com.unik.eventservice.entity.Event;
import com.unik.eventservice.entity.Notification;
import com.unik.eventservice.entity.NotificationType;
import com.unik.eventservice.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void eventInvitation(UUID recipientUserId, Event event) {
        notificationRepository.save(Notification.create(
                recipientUserId,
                NotificationType.EVENT_INVITATION,
                "You were invited to an event",
                "You were invited to \"" + event.getTitle() + "\".",
                event.getId()
        ));
    }

    @Transactional
    public void eventRegistration(UUID participantUserId, Event event) {
        notificationRepository.save(Notification.create(
                event.getOrganizerId(),
                NotificationType.EVENT_REGISTRATION,
                "New event registration",
                "A participant registered for \"" + event.getTitle() + "\".",
                event.getId()
        ));
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> list(UUID userId) {
        return notificationRepository.findAllByRecipientUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public NotificationResponse markRead(UUID userId, UUID notificationId) {
        Notification notification = notificationRepository.findByIdAndRecipientUserId(notificationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        notification.markRead();
        return toResponse(notificationRepository.save(notification));
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType().name(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getEventId(),
                notification.getReadAt() != null,
                notification.getReadAt(),
                notification.getCreatedAt()
        );
    }
}
