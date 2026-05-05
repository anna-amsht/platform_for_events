package com.unik.networkingservice.service;

import com.unik.networkingservice.dto.EventReportRequest;
import com.unik.networkingservice.dto.EventReportResponse;
import com.unik.networkingservice.entity.EventReport;
import com.unik.networkingservice.repository.EventReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class EventReportService {
    private final EventReportRepository repository;

    public EventReportService(EventReportRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Optional<EventReportResponse> get(UUID eventId) {
        return repository.findById(eventId).map(this::toResponse);
    }

    @Transactional
    public EventReportResponse save(UUID eventId, EventReportRequest request) {
        EventReport report = repository.findById(eventId)
                .orElseGet(() -> EventReport.create(eventId, request.authorId(), request.fileName(), request.contentType(), request.contentData()));
        report.update(request.authorId(), request.fileName(), request.contentType(), request.contentData());
        return toResponse(repository.save(report));
    }

    private EventReportResponse toResponse(EventReport report) {
        return new EventReportResponse(
                report.getEventId(),
                report.getAuthorId(),
                report.getFileName(),
                report.getContentType(),
                report.getContentData(),
                report.getCreatedAt(),
                report.getUpdatedAt()
        );
    }
}
