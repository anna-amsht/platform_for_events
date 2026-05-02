package com.unik.aichatservice.service;

import com.unik.aichatservice.config.AppProperties;
import com.unik.aichatservice.dto.ChatMessageResponse;
import com.unik.aichatservice.dto.ChatRequest;
import com.unik.aichatservice.dto.ChatSessionResponse;
import com.unik.aichatservice.entity.ChatMessage;
import com.unik.aichatservice.entity.ChatSession;
import com.unik.aichatservice.repository.ChatMessageRepository;
import com.unik.aichatservice.repository.ChatSessionRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class AiChatService {
    private final AppProperties properties;
    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final ObjectProvider<ChatClient.Builder> chatClientBuilderProvider;

    public AiChatService(AppProperties properties, ChatSessionRepository sessionRepository,
                         ChatMessageRepository messageRepository,
                         ObjectProvider<ChatClient.Builder> chatClientBuilderProvider) {
        this.properties = properties;
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.chatClientBuilderProvider = chatClientBuilderProvider;
    }

    @Transactional(readOnly = true)
    public List<ChatSessionResponse> sessions(UUID userId) {
        return sessionRepository.findAllByUserIdOrderByUpdatedAtDesc(userId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> history(UUID userId, UUID sessionId) {
        ChatSession session = loadSession(userId, sessionId);
        return messageRepository.findAllBySessionIdOrderByCreatedAtAsc(session.getId()).stream().map(this::toResponse).toList();
    }

    @Transactional
    public UUID acceptUserMessage(UUID userId, ChatRequest request) {
        ChatSession session = request.sessionId() == null
                ? sessionRepository.save(ChatSession.create(userId, titleFrom(request.message())))
                : loadSession(userId, request.sessionId());
        messageRepository.save(ChatMessage.create(session.getId(), "USER", request.message().trim()));
        session.touch();
        sessionRepository.save(session);
        return session.getId();
    }

    public Flux<String> streamAssistant(UUID sessionId, String userMessage) {
        Flux<String> stream = aiEnabled()
                ? chatClientBuilderProvider.getObject().build().prompt().user(userMessage).stream().content()
                : Flux.just("AI chat is disabled. Set APP_AI_ENABLED=true and OPENAI_API_KEY to enable Spring AI responses.");
        AtomicReference<StringBuilder> full = new AtomicReference<>(new StringBuilder());
        return stream.doOnNext(chunk -> full.get().append(chunk))
                .doOnComplete(() -> saveAssistantMessage(sessionId, full.get().toString()));
    }

    @Transactional
    public void saveAssistantMessage(UUID sessionId, String content) {
        messageRepository.save(ChatMessage.create(sessionId, "ASSISTANT", content));
    }

    private boolean aiEnabled() {
        return properties.ai().enabled() && chatClientBuilderProvider.getIfAvailable() != null;
    }

    private ChatSession loadSession(UUID userId, UUID sessionId) {
        return sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Chat session not found"));
    }

    private String titleFrom(String message) {
        String trimmed = message.trim();
        return trimmed.length() <= 80 ? trimmed : trimmed.substring(0, 80);
    }

    private ChatSessionResponse toResponse(ChatSession session) {
        return new ChatSessionResponse(session.getId(), session.getTitle(), session.getCreatedAt(), session.getUpdatedAt());
    }

    private ChatMessageResponse toResponse(ChatMessage message) {
        return new ChatMessageResponse(message.getId(), message.getSessionId(), message.getRole(),
                message.getContent(), message.getCreatedAt());
    }
}
