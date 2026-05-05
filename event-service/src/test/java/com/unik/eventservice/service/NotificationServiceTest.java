package com.unik.eventservice.service;

import com.unik.eventservice.entity.Event;
import com.unik.eventservice.entity.Invitation;
import com.unik.eventservice.entity.InvitationStatus;
import com.unik.eventservice.entity.Notification;
import com.unik.eventservice.entity.NotificationType;
import com.unik.eventservice.repository.InvitationRepository;
import com.unik.eventservice.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationServiceTest {
    private final NotificationRepository notificationRepository = mock(NotificationRepository.class);
    private final InvitationRepository invitationRepository = mock(InvitationRepository.class);
    private final NotificationService service = new NotificationService(notificationRepository, invitationRepository);

    @Test
    void eventInvitationCreatesInvitationNotification() {
        UUID recipientId = UUID.randomUUID();
        Event event = event(UUID.randomUUID());
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.eventInvitation(recipientId, event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getRecipientUserId()).isEqualTo(recipientId);
        assertThat(captor.getValue().getType()).isEqualTo(NotificationType.EVENT_INVITATION);
        assertThat(captor.getValue().getEventId()).isEqualTo(event.getId());
    }

    @Test
    void markReadReturnsReadNotificationWithInvitationStatus() {
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        Notification notification = Notification.create(
                userId,
                NotificationType.EVENT_INVITATION,
                "Invitation",
                "Message",
                eventId
        );
        Invitation invitation = Invitation.create(eventId, userId);
        invitation.accept();
        when(notificationRepository.findByIdAndRecipientUserId(notification.getId(), userId))
                .thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);
        when(invitationRepository.findByEventIdAndInviteeUserId(eventId, userId)).thenReturn(Optional.of(invitation));

        var response = service.markRead(userId, notification.getId());

        assertThat(response.read()).isTrue();
        assertThat(response.invitationStatus()).isEqualTo(InvitationStatus.ACCEPTED.name());
    }

    private static Event event(UUID organizerId) {
        return Event.create(
                organizerId,
                "Event",
                "Description",
                "Minsk",
                null,
                null,
                Instant.parse("2026-05-04T10:00:00Z"),
                Instant.parse("2026-05-04T11:00:00Z"),
                10
        );
    }
}
