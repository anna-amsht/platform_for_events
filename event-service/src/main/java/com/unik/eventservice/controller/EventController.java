package com.unik.eventservice.controller;

import com.unik.eventservice.dto.*;
import com.unik.eventservice.service.EventService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
public class EventController {
    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public List<EventResponse> all() {
        return eventService.findAll();
    }

    @PostMapping
    public EventResponse create(Authentication authentication, @Valid @RequestBody CreateEventRequest request) {
        return eventService.create(currentUserId(authentication), request);
    }

    @GetMapping("/{eventId}")
    public EventResponse get(@PathVariable UUID eventId) {
        return eventService.get(eventId);
    }

    @PatchMapping("/{eventId}")
    public EventResponse update(Authentication authentication, @PathVariable UUID eventId,
                                @Valid @RequestBody CreateEventRequest request) {
        return eventService.update(currentUserId(authentication), eventId, request);
    }

    @PostMapping("/{eventId}/invitations")
    public List<InvitationResponse> invite(Authentication authentication, @PathVariable UUID eventId,
                                           @Valid @RequestBody InviteUsersRequest request) {
        return eventService.invite(currentUserId(authentication), eventId, request);
    }

    @GetMapping("/{eventId}/invitations")
    public List<InvitationResponse> invitations(Authentication authentication, @PathVariable UUID eventId) {
        return eventService.invitations(currentUserId(authentication), eventId);
    }

    @PostMapping("/{eventId}/registrations")
    public RegistrationResponse register(Authentication authentication, @PathVariable UUID eventId) {
        return eventService.register(currentUserId(authentication), eventId);
    }

    @GetMapping("/registrations/me")
    public List<RegistrationResponse> myRegistrations(Authentication authentication) {
        return eventService.myRegistrations(currentUserId(authentication));
    }

    @GetMapping("/{eventId}/registrations")
    public List<RegistrationResponse> eventRegistrations(Authentication authentication, @PathVariable UUID eventId) {
        return eventService.eventRegistrations(currentUserId(authentication), eventId);
    }

    @PatchMapping("/{eventId}/registrations/{participantId}/role")
    public RegistrationResponse updateRegistrationRole(Authentication authentication,
                                                       @PathVariable UUID eventId,
                                                       @PathVariable UUID participantId,
                                                       @Valid @RequestBody UpdateRegistrationRoleRequest request) {
        return eventService.updateRegistrationRole(currentUserId(authentication), eventId, participantId, request);
    }

    @DeleteMapping("/{eventId}/registrations")
    public void cancelRegistration(Authentication authentication, @PathVariable UUID eventId) {
        eventService.cancelRegistration(currentUserId(authentication), eventId);
    }

    @DeleteMapping("/{eventId}")
    public void cancelEvent(Authentication authentication, @PathVariable UUID eventId) {
        eventService.cancelEvent(currentUserId(authentication), eventId);
    }

    @PostMapping("/{eventId}/invitations/accept")
    public RegistrationResponse acceptInvitation(Authentication authentication, @PathVariable UUID eventId) {
        return eventService.acceptInvitation(currentUserId(authentication), eventId);
    }

    @PostMapping("/{eventId}/invitations/decline")
    public InvitationResponse declineInvitation(Authentication authentication, @PathVariable UUID eventId) {
        return eventService.declineInvitation(currentUserId(authentication), eventId);
    }

    private UUID currentUserId(Authentication authentication) {
        return (UUID) authentication.getDetails();
    }
}
