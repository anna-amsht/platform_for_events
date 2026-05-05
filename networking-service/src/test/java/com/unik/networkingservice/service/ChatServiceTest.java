package com.unik.networkingservice.service;

import com.unik.networkingservice.dto.ChatMessageRequest;
import com.unik.networkingservice.entity.ChatMessage;
import com.unik.networkingservice.repository.ChatMessageRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatServiceTest {
    private final ChatMessageRepository repository = mock(ChatMessageRepository.class);
    private final ChatService service = new ChatService(repository);

    @Test
    void sendNormalizesRoomAndContent() {
        UUID eventId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        when(repository.save(any(ChatMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.send(eventId, " listener ", new ChatMessageRequest(senderId, " hello "));

        assertThat(response.eventId()).isEqualTo(eventId);
        assertThat(response.roleName()).isEqualTo("LISTENER");
        assertThat(response.senderId()).isEqualTo(senderId);
        assertThat(response.content()).isEqualTo("hello");
    }

    @Test
    void historyClampsLimitToRepositoryPageSize() {
        UUID eventId = UUID.randomUUID();
        when(repository.findAllByEventIdAndRoleNameOrderByCreatedAtAsc(any(), any(), any())).thenReturn(List.of());

        service.history(eventId, "speaker", 500);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAllByEventIdAndRoleNameOrderByCreatedAtAsc(
                org.mockito.Mockito.eq(eventId),
                org.mockito.Mockito.eq("SPEAKER"),
                pageableCaptor.capture()
        );
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(200);
    }
}
