package com.unik.eventservice.controller;

import com.unik.eventservice.dto.NotificationResponse;
import com.unik.eventservice.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationResponse> list(Authentication authentication) {
        return notificationService.list(currentUserId(authentication));
    }

    @PatchMapping("/{notificationId}/read")
    public NotificationResponse markRead(Authentication authentication, @PathVariable UUID notificationId) {
        return notificationService.markRead(currentUserId(authentication), notificationId);
    }

    private UUID currentUserId(Authentication authentication) {
        return (UUID) authentication.getDetails();
    }
}
