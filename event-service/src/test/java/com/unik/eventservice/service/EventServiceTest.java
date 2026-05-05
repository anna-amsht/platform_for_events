package com.unik.eventservice.service;

import com.unik.eventservice.dto.CreateEventRequest;
import com.unik.eventservice.dto.UpdateRegistrationRoleRequest;
import com.unik.eventservice.entity.Event;
import com.unik.eventservice.entity.EventRegistration;
import com.unik.eventservice.repository.EventRegistrationRepository;
import com.unik.eventservice.repository.EventRepository;
import com.unik.eventservice.repository.InvitationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventServiceTest {
    private final EventRepository eventRepository = mock(EventRepository.class);
    private final InvitationRepository invitationRepository = mock(InvitationRepository.class);
    private final EventRegistrationRepository registrationRepository = mock(EventRegistrationRepository.class);
    private final NotificationService notificationService = mock(NotificationService.class);
    private final EventService service = new EventService(eventRepository, invitationRepository, registrationRepository, notificationService);

    @Test
    void createRejectsEndBeforeStart() {
        Instant startsAt = Instant.parse("2026-05-04T10:00:00Z");

        assertThatThrownBy(() -> service.create(UUID.randomUUID(), new CreateEventRequest(
                "Event", null, null, null, null, startsAt, startsAt, 10)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Event end time must be after start time");
    }

    @Test
    void organizerCannotRegisterForOwnEvent() {
        UUID organizerId = UUID.randomUUID();
        Event event = event(organizerId);
        when(eventRepository.findByIdForUpdate(event.getId())).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> service.register(organizerId, event.getId()))
                .isInstanceOf(AccessDeniedException.class);

        verify(registrationRepository, never()).save(any());
    }

    @Test
    void registerReservesSeatForNewParticipant() {
        UUID participantId = UUID.randomUUID();
        Event event = event(UUID.randomUUID());
        when(eventRepository.findByIdForUpdate(event.getId())).thenReturn(Optional.of(event));
        when(registrationRepository.findByEventIdAndUserId(event.getId(), participantId)).thenReturn(Optional.empty());
        EventRegistration registration = EventRegistration.create(event.getId(), participantId, "LISTENER");
        when(registrationRepository.save(any())).thenReturn(registration);

        service.register(participantId, event.getId());

        assertThat(event.getAvailableSeats()).isEqualTo(9);
        verify(notificationService).eventRegistration(participantId, event);
    }

    @Test
    void registerRejectsWhenNoSeatsLeft() {
        UUID participantId = UUID.randomUUID();
        Event event = Event.create(
                UUID.randomUUID(),
                "Event",
                "Description",
                "Minsk",
                null,
                null,
                Instant.parse("2026-05-04T10:00:00Z"),
                Instant.parse("2026-05-04T11:00:00Z"),
                1
        );
        event.reserveSeat();
        when(eventRepository.findByIdForUpdate(event.getId())).thenReturn(Optional.of(event));
        when(registrationRepository.findByEventIdAndUserId(event.getId(), participantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.register(participantId, event.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No available seats for event");

        verify(registrationRepository, never()).save(any());
    }

    @Test
    void leaderCanUpdateParticipantRoleAndNotificationIsSent() {
        UUID leaderId = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();
        Event event = event(UUID.randomUUID());
        EventRegistration leader = EventRegistration.create(event.getId(), leaderId, "LEADER");
        EventRegistration participant = EventRegistration.create(event.getId(), participantId, "LISTENER");
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(registrationRepository.findByEventIdAndUserId(event.getId(), leaderId)).thenReturn(Optional.of(leader));
        when(registrationRepository.findByEventIdAndUserId(event.getId(), participantId)).thenReturn(Optional.of(participant));
        when(registrationRepository.save(participant)).thenReturn(participant);

        var response = service.updateRegistrationRole(leaderId, event.getId(), participantId, new UpdateRegistrationRoleRequest(" speaker "));

        assertThat(response.roleName()).isEqualTo("SPEAKER");
        verify(notificationService).eventRoleChanged(participantId, event, "SPEAKER");
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
