package com.unik.networkingservice.controller;

import com.unik.networkingservice.dto.ChatMessageRequest;
import com.unik.networkingservice.dto.ChatMessageResponse;
import com.unik.networkingservice.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/rooms")
public class ChatController {
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping("/available/{roleName}")
    public List<String> availableRooms(@PathVariable String roleName) {
        return chatService.availableRooms(roleName);
    }

    @GetMapping("/{eventId}/{roleName}/messages")
    public List<ChatMessageResponse> history(@PathVariable UUID eventId, @PathVariable String roleName,
                                             @RequestParam(defaultValue = "50") int limit) {
        return chatService.history(eventId, roleName, limit);
    }

    @PostMapping("/{eventId}/{roleName}/messages")
    public ChatMessageResponse sendRest(@PathVariable UUID eventId, @PathVariable String roleName,
                                        @Valid @RequestBody ChatMessageRequest request) {
        return chatService.send(eventId, roleName, request);
    }

    @GetMapping("/{eventId}/general/messages")
    public List<ChatMessageResponse> generalHistory(@PathVariable UUID eventId,
                                                    @RequestParam(defaultValue = "50") int limit) {
        return chatService.history(eventId, ChatService.GENERAL_ROOM, limit);
    }

    @PostMapping("/{eventId}/general/messages")
    public ChatMessageResponse sendGeneralRest(@PathVariable UUID eventId,
                                               @Valid @RequestBody ChatMessageRequest request) {
        return chatService.send(eventId, ChatService.GENERAL_ROOM, request);
    }

    @MessageMapping("/rooms/{eventId}/{roleName}")
    public void sendWs(@DestinationVariable UUID eventId, @DestinationVariable String roleName,
                       @Valid ChatMessageRequest request) {
        ChatMessageResponse response = chatService.send(eventId, roleName, request);
        broadcast(eventId, roleName, response);
    }

    @MessageMapping("/rooms/{eventId}/general")
    public void sendGeneralWs(@DestinationVariable UUID eventId, @Valid ChatMessageRequest request) {
        ChatMessageResponse response = chatService.send(eventId, ChatService.GENERAL_ROOM, request);
        broadcast(eventId, "general", response);
    }

    private void broadcast(UUID eventId, String requestedRoom, ChatMessageResponse response) {
        String canonicalDestination = "/topic/rooms/" + eventId + "/" + response.roleName();
        messagingTemplate.convertAndSend(canonicalDestination, response);

        String requestedDestination = "/topic/rooms/" + eventId + "/" + requestedRoom;
        if (!canonicalDestination.equals(requestedDestination)) {
            messagingTemplate.convertAndSend(requestedDestination, response);
        }
    }
}
