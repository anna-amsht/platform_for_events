package com.unik.networkingservice.controller;

import com.unik.networkingservice.dto.ChatMessageRequest;
import com.unik.networkingservice.dto.ChatMessageResponse;
import com.unik.networkingservice.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatControllerTest {
    @Test
    void websocketMessageIsPublishedToCanonicalAndRequestedRoomDestinations() {
        ChatService chatService = mock(ChatService.class);
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
        ChatController controller = new ChatController(chatService, messagingTemplate);

        UUID eventId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        ChatMessageRequest request = new ChatMessageRequest(senderId, "hello");
        ChatMessageResponse response = new ChatMessageResponse(
                UUID.randomUUID(), eventId, "LISTENER", senderId, "hello", Instant.now());
        when(chatService.send(eventId, "listener", request)).thenReturn(response);

        controller.sendWs(eventId, "listener", request);

        verify(messagingTemplate).convertAndSend("/topic/rooms/" + eventId + "/LISTENER", response);
        verify(messagingTemplate).convertAndSend("/topic/rooms/" + eventId + "/listener", response);
    }
}
