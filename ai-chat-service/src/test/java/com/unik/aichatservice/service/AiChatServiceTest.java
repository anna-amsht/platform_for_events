package com.unik.aichatservice.service;

import com.unik.aichatservice.dto.ChatRequest;
import com.unik.aichatservice.entity.ChatMessage;
import com.unik.aichatservice.entity.ChatSession;
import com.unik.aichatservice.repository.ChatMessageRepository;
import com.unik.aichatservice.repository.ChatSessionRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiChatServiceTest {
    private final ChatSessionRepository sessionRepository = mock(ChatSessionRepository.class);
    private final ChatMessageRepository messageRepository = mock(ChatMessageRepository.class);
    private final InstructionAnswerService instructionAnswerService = mock(InstructionAnswerService.class);
    private final AiChatService service = new AiChatService(sessionRepository, messageRepository, instructionAnswerService);

    @Test
    void acceptUserMessageCreatesSessionAndTrimsUserMessage() {
        UUID userId = UUID.randomUUID();
        when(sessionRepository.save(any(ChatSession.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(messageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UUID sessionId = service.acceptUserMessage(userId, new ChatRequest(null, " hello "));

        assertThat(sessionId).isNotNull();
        ArgumentCaptor<ChatMessage> messageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(messageRepository).save(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getSessionId()).isEqualTo(sessionId);
        assertThat(messageCaptor.getValue().getRole()).isEqualTo("USER");
        assertThat(messageCaptor.getValue().getContent()).isEqualTo("hello");
    }

    @Test
    void acceptUserMessageRejectsSessionOwnedByAnotherUser() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        when(sessionRepository.findByIdAndUserId(sessionId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.acceptUserMessage(userId, new ChatRequest(sessionId, "hello")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Chat session not found");
    }

    @Test
    void streamAssistantPersistsInstructionAnswer() {
        UUID sessionId = UUID.randomUUID();
        when(instructionAnswerService.answer("how to use map")).thenReturn("Open the map.");
        when(messageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String answer = service.streamAssistant(sessionId, "how to use map");

        assertThat(answer).isEqualTo("Open the map.");
        ArgumentCaptor<ChatMessage> messageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(messageRepository).save(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getRole()).isEqualTo("ASSISTANT");
        assertThat(messageCaptor.getValue().getContent()).isEqualTo("Open the map.");
    }
}
