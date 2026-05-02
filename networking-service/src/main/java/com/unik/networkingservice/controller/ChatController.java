package com.unik.networkingservice.controller;

import com.unik.networkingservice.dto.ChatMessageRequest;
import com.unik.networkingservice.dto.ChatMessageResponse;
import com.unik.networkingservice.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/rooms")
public class ChatController {
    private final ChatService chatService;
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
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
    @SendTo("/topic/rooms/{eventId}/{roleName}")
    public ChatMessageResponse sendWs(@DestinationVariable UUID eventId, @DestinationVariable String roleName,
                                      @Valid ChatMessageRequest request) {
        return chatService.send(eventId, roleName, request);
    }

    @MessageMapping("/rooms/{eventId}/general")
    @SendTo("/topic/rooms/{eventId}/general")
    public ChatMessageResponse sendGeneralWs(@DestinationVariable UUID eventId,
                                             @Valid ChatMessageRequest request) {
        return chatService.send(eventId, ChatService.GENERAL_ROOM, request);
    }
}
