package com.unik.networkingservice.controller;

import com.unik.networkingservice.dto.EventReportRequest;
import com.unik.networkingservice.dto.EventReportResponse;
import com.unik.networkingservice.service.EventReportService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/reports")
public class EventReportController {
    private final EventReportService service;

    public EventReportController(EventReportService service) {
        this.service = service;
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventReportResponse> get(@PathVariable UUID eventId) {
        return service.get(eventId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{eventId}")
    public EventReportResponse save(@PathVariable UUID eventId, @Valid @RequestBody EventReportRequest request) {
        return service.save(eventId, request);
    }
}
