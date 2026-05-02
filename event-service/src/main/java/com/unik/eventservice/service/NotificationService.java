package com.unik.eventservice.service;

import com.unik.eventservice.dto.NotificationResponse;
import com.unik.eventservice.entity.Event;
import com.unik.eventservice.entity.Notification;
import com.unik.eventservice.entity.NotificationType;
import com.unik.eventservice.repository.InvitationRepository;
import com.unik.eventservice.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final InvitationRepository invitationRepository;

    public NotificationService(NotificationRepository notificationRepository, InvitationRepository invitationRepository) {
        this.notificationRepository = notificationRepository;
        this.invitationRepository = invitationRepository;
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

    @Transactional
    public void eventCancelled(UUID participantUserId, Event event) {
        notificationRepository.save(Notification.create(
                participantUserId,
                NotificationType.EVENT_CANCELLED,
                "Event was cancelled",
                "Event \"" + event.getTitle() + "\" was cancelled by the organizer.",
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
                invitationStatus(notification),
                notification.getReadAt() != null,
                notification.getReadAt(),
                notification.getCreatedAt()
        );
    }

    private String invitationStatus(Notification notification) {
        if (notification.getType() != NotificationType.EVENT_INVITATION || notification.getEventId() == null) {
            return null;
        }
        return invitationRepository.findByEventIdAndInviteeUserId(
                        notification.getEventId(),
                        notification.getRecipientUserId()
                )
                .map(invitation -> invitation.getStatus().name())
                .orElse(null);
    }
}
