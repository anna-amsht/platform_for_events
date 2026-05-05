package com.unik.networkingservice.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChatRoomTypeTest {
    @Test
    void speakerCanAccessGeneralSpeakerAndListenerRooms() {
        assertThat(ChatRoomType.availableFor("SPEAKER"))
                .containsExactly("GENERAL", "SPEAKER", "LISTENER");
    }

    @Test
    void otherRolesCanAccessGeneralAndOwnRoom() {
        assertThat(ChatRoomType.availableFor("LISTENER"))
                .containsExactlyElementsOf(List.of("GENERAL", "LISTENER"));
    }
}
