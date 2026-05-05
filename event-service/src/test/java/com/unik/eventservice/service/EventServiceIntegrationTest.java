package com.unik.eventservice.service;

import com.unik.eventservice.dto.CreateEventRequest;
import com.unik.eventservice.dto.UpdateRegistrationRoleRequest;
import com.unik.eventservice.entity.EventRegistration;
import com.unik.eventservice.repository.EventRegistrationRepository;
import com.unik.eventservice.repository.EventRepository;
import com.unik.eventservice.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class EventServiceIntegrationTest {
    @Autowired
    private EventService eventService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventRegistrationRepository registrationRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void leaderCanChangeRoleThroughPersistedRepositories() {
        UUID organizerId = UUID.randomUUID();
        UUID leaderId = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();
        var event = eventService.create(organizerId, eventRequest());
        registrationRepository.save(EventRegistration.create(event.id(), leaderId, "LEADER"));
        registrationRepository.save(EventRegistration.create(event.id(), participantId, "LISTENER"));

        var response = eventService.updateRegistrationRole(
                leaderId,
                event.id(),
                participantId,
                new UpdateRegistrationRoleRequest("speaker")
        );

        assertThat(response.roleName()).isEqualTo("SPEAKER");
        assertThat(registrationRepository.findByEventIdAndUserId(event.id(), participantId))
                .get()
                .extracting(EventRegistration::getRoleName)
                .isEqualTo("SPEAKER");
        assertThat(notificationRepository.findAll()).hasSize(1);
        assertThat(eventRepository.findById(event.id())).isPresent();
    }

    private static CreateEventRequest eventRequest() {
        return new CreateEventRequest(
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
