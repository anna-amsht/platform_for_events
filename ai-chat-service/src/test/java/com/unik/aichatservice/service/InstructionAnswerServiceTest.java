package com.unik.aichatservice.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InstructionAnswerServiceTest {
    private final InstructionAnswerService service = new InstructionAnswerService();

    @Test
    void answerSelectsGithubInstructionByKeyword() {
        String answer = service.answer("github");

        assertThat(answer.toLowerCase()).contains("github");
    }

    @Test
    void answerFallsBackForUnknownQuestion() {
        String answer = service.answer("unknown topic");

        assertThat(answer).isNotBlank();
    }

    @Test
    void answerGeneratesEventDescriptionByOrganizerKeyword() {
        String answer = service.answer("Помоги составить описание мероприятия для Java-разработчиков");

        assertThat(answer).contains("Черновик описания");
        assertThat(answer).contains("нетворкинг");
    }

    @Test
    void answerSuggestsEventProgramByKeyword() {
        String answer = service.answer("Предложи программу и тайминг мероприятия");

        assertThat(answer).contains("Рекомендуемая программа");
        assertThat(answer).contains("вопросы");
    }
}
