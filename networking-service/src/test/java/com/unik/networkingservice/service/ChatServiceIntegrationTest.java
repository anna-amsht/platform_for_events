package com.unik.networkingservice.service;

import com.unik.networkingservice.dto.ChatMessageRequest;
import com.unik.networkingservice.repository.ChatMessageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ChatServiceIntegrationTest {
    @Autowired
    private ChatService chatService;

    @Autowired
    private ChatMessageRepository repository;

    @Test
    void sendAndHistoryRoundTripThroughRepository() {
        UUID eventId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();

        chatService.send(eventId, "listener", new ChatMessageRequest(senderId, "hello"));

        var history = chatService.history(eventId, "LISTENER", 50);
        assertThat(history).hasSize(1);
        assertThat(history.getFirst().roleName()).isEqualTo("LISTENER");
        assertThat(history.getFirst().content()).isEqualTo("hello");
        assertThat(repository.findAll()).hasSize(1);
    }
}
