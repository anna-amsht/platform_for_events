package com.unik.aichatservice.controller;

import com.unik.aichatservice.dto.ChatMessageResponse;
import com.unik.aichatservice.dto.ChatRequest;
import com.unik.aichatservice.dto.ChatSessionResponse;
import com.unik.aichatservice.service.AiChatService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
public class AiChatController {
    private final AiChatService aiChatService;
    public AiChatController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    @GetMapping("/sessions")
    public List<ChatSessionResponse> sessions(Authentication authentication) {
        return aiChatService.sessions(currentUserId(authentication));
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public List<ChatMessageResponse> history(Authentication authentication, @PathVariable UUID sessionId) {
        return aiChatService.history(currentUserId(authentication), sessionId);
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_PLAIN_VALUE)
    public String stream(Authentication authentication, @Valid @RequestBody ChatRequest request) {
        UUID sessionId = aiChatService.acceptUserMessage(currentUserId(authentication), request);
        return aiChatService.streamAssistant(sessionId, request.message());
    }

    private UUID currentUserId(Authentication authentication) {
        return (UUID) authentication.getDetails();
    }
}
