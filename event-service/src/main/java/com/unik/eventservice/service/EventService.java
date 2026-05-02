package com.unik.eventservice.service;

import com.unik.eventservice.dto.*;
import com.unik.eventservice.entity.Event;
import com.unik.eventservice.entity.EventRegistration;
import com.unik.eventservice.entity.Invitation;
import com.unik.eventservice.repository.EventRegistrationRepository;
import com.unik.eventservice.repository.EventRepository;
import com.unik.eventservice.repository.InvitationRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class EventService {
    private static final List<String> ROLES = List.of("LEADER", "SPEAKER", "INFORMER", "LISTENER");
    private final EventRepository eventRepository;
    private final InvitationRepository invitationRepository;
    private final EventRegistrationRepository registrationRepository;

    public EventService(EventRepository eventRepository, InvitationRepository invitationRepository,
                        EventRegistrationRepository registrationRepository) {
        this.eventRepository = eventRepository;
        this.invitationRepository = invitationRepository;
        this.registrationRepository = registrationRepository;
    }

    @Transactional(readOnly = true)
    public List<EventResponse> findAll() {
        return eventRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public EventResponse get(UUID eventId) {
        return toResponse(load(eventId));
    }

    @Transactional
    public EventResponse create(UUID organizerId, CreateEventRequest request) {
        validateDates(request);
        return toResponse(eventRepository.save(Event.create(organizerId, request.title(), request.description(),
                request.location(), request.latitude(), request.longitude(), request.startsAt(), request.endsAt())));
    }

    @Transactional
    public EventResponse update(UUID userId, UUID eventId, CreateEventRequest request) {
        validateDates(request);
        Event event = load(eventId);
        requireOrganizer(userId, event);
        event.update(request.title(), request.description(), request.location(), request.latitude(),
                request.longitude(), request.startsAt(), request.endsAt());
        return toResponse(eventRepository.save(event));
    }

    @Transactional
    public List<InvitationResponse> invite(UUID userId, UUID eventId, InviteUsersRequest request) {
        Event event = load(eventId);
        requireOrganizer(userId, event);
        List<Invitation> invitations = request.userIds().stream()
                .filter(inviteeId -> !invitationRepository.existsByEventIdAndInviteeUserId(eventId, inviteeId))
                .map(inviteeId -> Invitation.create(eventId, inviteeId))
                .toList();
        return invitationRepository.saveAll(invitations).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<InvitationResponse> invitations(UUID userId, UUID eventId) {
        requireOrganizer(userId, load(eventId));
        return invitationRepository.findAllByEventId(eventId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public RegistrationResponse register(UUID userId, UUID eventId) {
        Event event = load(eventId);
        if (event.getOrganizerId().equals(userId)) {
            throw new AccessDeniedException("Event organizer cannot register as participant for own event");
        }
        EventRegistration registration = registrationRepository.findByEventIdAndUserId(eventId, userId)
                .orElseGet(() -> EventRegistration.create(eventId, userId, randomRole()));
        return toResponse(registrationRepository.save(registration));
    }

    private Event load(UUID eventId) {
        return eventRepository.findById(eventId).orElseThrow(() -> new IllegalArgumentException("Event not found"));
    }

    private void validateDates(CreateEventRequest request) {
        if (!request.endsAt().isAfter(request.startsAt())) {
            throw new IllegalArgumentException("Event end time must be after start time");
        }
    }

    private void requireOrganizer(UUID userId, Event event) {
        if (!event.getOrganizerId().equals(userId)) {
            throw new AccessDeniedException("Only event organizer can perform this operation");
        }
    }

    private String randomRole() {
        return ROLES.get(ThreadLocalRandom.current().nextInt(ROLES.size()));
    }

    private EventResponse toResponse(Event event) {
        return new EventResponse(event.getId(), event.getOrganizerId(), event.getTitle(), event.getDescription(),
                event.getLocation(), event.getLatitude(), event.getLongitude(), event.getStartsAt(), event.getEndsAt(),
                event.getCreatedAt(), event.getUpdatedAt());
    }

    private InvitationResponse toResponse(Invitation invitation) {
        return new InvitationResponse(invitation.getId(), invitation.getEventId(), invitation.getInviteeUserId(),
                invitation.getStatus().name(), invitation.getCreatedAt());
    }

    private RegistrationResponse toResponse(EventRegistration registration) {
        return new RegistrationResponse(registration.getId(), registration.getEventId(), registration.getUserId(),
                registration.getRoleName(), registration.getCreatedAt());
    }
}
