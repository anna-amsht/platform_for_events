package com.unik.networkingservice.service;

import com.unik.networkingservice.dto.ChatMessageRequest;
import com.unik.networkingservice.dto.ChatMessageResponse;
import com.unik.networkingservice.entity.ChatMessage;
import com.unik.networkingservice.repository.ChatMessageRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ChatService {
    public static final String GENERAL_ROOM = "GENERAL";

    private final ChatMessageRepository repository;
    public ChatService(ChatMessageRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ChatMessageResponse send(UUID eventId, String roleName, ChatMessageRequest request) {
        return toResponse(repository.save(ChatMessage.create(eventId, normalizeRole(roleName), request.senderId(), request.content())));
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> history(UUID eventId, String roleName, int limit) {
        return repository.findAllByEventIdAndRoleNameOrderByCreatedAtAsc(
                eventId, normalizeRole(roleName), PageRequest.of(0, Math.min(Math.max(limit, 1), 200)))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private String normalizeRole(String roleName) {
        return ChatRoomType.from(roleName.trim()).name();
    }

    private ChatMessageResponse toResponse(ChatMessage message) {
        return new ChatMessageResponse(message.getId(), message.getEventId(), message.getRoleName(),
                message.getSenderId(), message.getContent(), message.getCreatedAt());
    }
}
