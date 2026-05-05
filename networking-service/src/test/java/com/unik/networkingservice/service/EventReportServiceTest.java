package com.unik.networkingservice.service;

import com.unik.networkingservice.dto.EventReportRequest;
import com.unik.networkingservice.entity.EventReport;
import com.unik.networkingservice.repository.EventReportRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EventReportServiceTest {
    private final EventReportRepository repository = mock(EventReportRepository.class);
    private final EventReportService service = new EventReportService(repository);

    @Test
    void saveCreatesReportWhenMissing() {
        UUID eventId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        when(repository.findById(eventId)).thenReturn(Optional.empty());
        when(repository.save(any(EventReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.save(eventId, new EventReportRequest(authorId, " report.pdf ", " application/pdf ", " data "));

        assertThat(response.eventId()).isEqualTo(eventId);
        assertThat(response.authorId()).isEqualTo(authorId);
        assertThat(response.fileName()).isEqualTo("report.pdf");
        assertThat(response.contentType()).isEqualTo("application/pdf");
        assertThat(response.contentData()).isEqualTo("data");
    }

    @Test
    void saveUpdatesExistingReport() {
        UUID eventId = UUID.randomUUID();
        UUID oldAuthorId = UUID.randomUUID();
        UUID newAuthorId = UUID.randomUUID();
        EventReport report = EventReport.create(eventId, oldAuthorId, "old.txt", "text/plain", "old");
        when(repository.findById(eventId)).thenReturn(Optional.of(report));
        when(repository.save(report)).thenReturn(report);

        var response = service.save(eventId, new EventReportRequest(newAuthorId, "new.txt", "text/plain", "new"));

        assertThat(response.authorId()).isEqualTo(newAuthorId);
        assertThat(response.fileName()).isEqualTo("new.txt");
        assertThat(response.contentData()).isEqualTo("new");
    }
}
