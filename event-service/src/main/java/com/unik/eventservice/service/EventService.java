package com.unik.eventservice.service;

import com.unik.eventservice.dto.*;
import com.unik.eventservice.entity.Event;
import com.unik.eventservice.entity.EventRegistration;
import com.unik.eventservice.entity.Invitation;
import com.unik.eventservice.entity.InvitationStatus;
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
    private final NotificationService notificationService;

    public EventService(EventRepository eventRepository, InvitationRepository invitationRepository,
                        EventRegistrationRepository registrationRepository,
                        NotificationService notificationService) {
        this.eventRepository = eventRepository;
        this.invitationRepository = invitationRepository;
        this.registrationRepository = registrationRepository;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<EventResponse> findAll() {
        return eventRepository.findAllByCancelledAtIsNull().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public EventResponse get(UUID eventId) {
        return toResponse(loadActive(eventId));
    }

    @Transactional
    public EventResponse create(UUID organizerId, CreateEventRequest request) {
        validateDates(request);
        return toResponse(eventRepository.save(Event.create(organizerId, request.title(), request.description(),
                request.location(), request.latitude(), request.longitude(), request.startsAt(), request.endsAt(),
                request.capacity())));
    }

    @Transactional
    public EventResponse update(UUID userId, UUID eventId, CreateEventRequest request) {
        validateDates(request);
        Event event = loadActive(eventId);
        requireOrganizer(userId, event);
        long registeredParticipants = registrationRepository.countByEventId(eventId);
        event.update(request.title(), request.description(), request.location(), request.latitude(),
                request.longitude(), request.startsAt(), request.endsAt(), request.capacity(), registeredParticipants);
        return toResponse(eventRepository.save(event));
    }

    @Transactional
    public List<InvitationResponse> invite(UUID userId, UUID eventId, InviteUsersRequest request) {
        Event event = loadActive(eventId);
        requireOrganizer(userId, event);
        List<Invitation> invitations = request.userIds().stream()
                .filter(inviteeId -> !invitationRepository.existsByEventIdAndInviteeUserId(eventId, inviteeId))
                .map(inviteeId -> Invitation.create(eventId, inviteeId))
                .toList();
        List<Invitation> saved = invitationRepository.saveAll(invitations);
        saved.forEach(invitation -> notificationService.eventInvitation(invitation.getInviteeUserId(), event));
        return saved.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<InvitationResponse> invitations(UUID userId, UUID eventId) {
        requireOrganizer(userId, loadActive(eventId));
        return invitationRepository.findAllByEventId(eventId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public RegistrationResponse register(UUID userId, UUID eventId) {
        Event event = loadActiveForUpdate(eventId);
        if (event.getOrganizerId().equals(userId)) {
            throw new AccessDeniedException("Event organizer cannot register as participant for own event");
        }
        return registrationRepository.findByEventIdAndUserId(eventId, userId)
                .map(this::toResponse)
                .orElseGet(() -> {
                    event.reserveSeat();
                    EventRegistration registration = registrationRepository.save(
                            EventRegistration.create(eventId, userId, randomRole()));
                    notificationService.eventRegistration(userId, event);
                    return toResponse(registration);
                });
    }

    @Transactional(readOnly = true)
    public List<RegistrationResponse> myRegistrations(UUID userId) {
        return registrationRepository.findAllByUserId(userId).stream()
                .filter(registration -> eventRepository.findById(registration.getEventId())
                        .map(event -> !event.isCancelled())
                        .orElse(false))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RegistrationResponse> eventRegistrations(UUID userId, UUID eventId) {
        loadActive(eventId);
        requireLeader(userId, eventId);
        return registrationRepository.findAllByEventId(eventId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public RegistrationResponse updateRegistrationRole(UUID userId, UUID eventId, UUID participantId,
                                                       UpdateRegistrationRoleRequest request) {
        Event event = loadActive(eventId);
        requireLeader(userId, eventId);
        if (userId.equals(participantId)) {
            throw new AccessDeniedException("Leader cannot change own role");
        }
        String roleName = request.roleName().trim().toUpperCase();
        if (!ROLES.contains(roleName)) {
            throw new IllegalArgumentException("Unsupported event role");
        }
        EventRegistration registration = registrationRepository.findByEventIdAndUserId(eventId, participantId)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found"));
        registration.changeRole(roleName);
        EventRegistration saved = registrationRepository.save(registration);
        notificationService.eventRoleChanged(participantId, event, roleName);
        return toResponse(saved);
    }

    @Transactional
    public void cancelRegistration(UUID userId, UUID eventId) {
        Event event = loadActiveForUpdate(eventId);
        EventRegistration registration = registrationRepository.findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found"));
        registrationRepository.delete(registration);
        event.releaseSeat();
    }

    @Transactional
    public void cancelEvent(UUID userId, UUID eventId) {
        Event event = loadActive(eventId);
        requireOrganizer(userId, event);
        List<EventRegistration> registrations = registrationRepository.findAllByEventId(eventId);
        registrations.forEach(registration -> notificationService.eventCancelled(registration.getUserId(), event));
        registrationRepository.deleteAll(registrations);
        event.cancel();
        eventRepository.save(event);
    }

    @Transactional
    public RegistrationResponse acceptInvitation(UUID userId, UUID eventId) {
        loadActive(eventId);
        Invitation invitation = invitationRepository.findByEventIdAndInviteeUserId(eventId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));
        if (invitation.getStatus() == InvitationStatus.DECLINED) {
            throw new IllegalArgumentException("Invitation was declined");
        }
        if (invitation.getStatus() == InvitationStatus.ACCEPTED) {
            return register(userId, eventId);
        }
        invitation.accept();
        invitationRepository.save(invitation);
        return register(userId, eventId);
    }

    @Transactional
    public InvitationResponse declineInvitation(UUID userId, UUID eventId) {
        loadActive(eventId);
        Invitation invitation = invitationRepository.findByEventIdAndInviteeUserId(eventId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));
        if (invitation.getStatus() == InvitationStatus.ACCEPTED) {
            throw new IllegalArgumentException("Invitation already accepted");
        }
        invitation.decline();
        return toResponse(invitationRepository.save(invitation));
    }

    private Event load(UUID eventId) {
        return eventRepository.findById(eventId).orElseThrow(() -> new IllegalArgumentException("Event not found"));
    }

    private Event loadActive(UUID eventId) {
        Event event = load(eventId);
        if (event.isCancelled()) {
            throw new IllegalArgumentException("Event was cancelled");
        }
        return event;
    }

    private Event loadActiveForUpdate(UUID eventId) {
        Event event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        if (event.isCancelled()) {
            throw new IllegalArgumentException("Event was cancelled");
        }
        return event;
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

    private void requireLeader(UUID userId, UUID eventId) {
        EventRegistration registration = registrationRepository.findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new AccessDeniedException("Only event leader can perform this operation"));
        if (!"LEADER".equals(registration.getRoleName())) {
            throw new AccessDeniedException("Only event leader can perform this operation");
        }
    }

    private String randomRole() {
        return ROLES.get(ThreadLocalRandom.current().nextInt(ROLES.size()));
    }

    private EventResponse toResponse(Event event) {
        return new EventResponse(event.getId(), event.getOrganizerId(), event.getTitle(), event.getDescription(),
                event.getLocation(), event.getLatitude(), event.getLongitude(), event.getStartsAt(), event.getEndsAt(),
                event.getCapacity(), event.getAvailableSeats(), event.getCreatedAt(), event.getUpdatedAt());
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
