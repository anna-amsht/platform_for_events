package com.unik.eventservice.controller;

import com.unik.eventservice.dto.*;
import com.unik.eventservice.service.EventService;
import com.unik.eventservice.service.WeatherService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
public class EventController {
    private final EventService eventService;
    private final WeatherService weatherService;

    public EventController(EventService eventService, WeatherService weatherService) {
        this.eventService = eventService;
        this.weatherService = weatherService;
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

    @GetMapping("/{eventId}/weather")
    public WeatherForecastResponse weather(@PathVariable UUID eventId) {
        return weatherService.forecast(eventId);
    }

    private UUID currentUserId(Authentication authentication) {
        return (UUID) authentication.getDetails();
    }
}
