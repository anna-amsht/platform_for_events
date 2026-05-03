package com.unik.networkingservice.service;

import java.util.Arrays;
import java.util.List;

public enum ChatRoomType {
    GENERAL,
    LEADER,
    SPEAKER,
    INFORMER,
    LISTENER;

    public static ChatRoomType from(String value) {
        return Arrays.stream(values())
                .filter(type -> type.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported chat room: " + value));
    }

    public static List<String> availableFor(String roleName) {
        ChatRoomType roleRoom = from(roleName.trim());
        if (roleRoom == GENERAL) {
            return List.of(GENERAL.name());
        }
        if (roleRoom == SPEAKER) {
            return List.of(GENERAL.name(), SPEAKER.name(), LISTENER.name());
        }
        return List.of(GENERAL.name(), roleRoom.name());
    }
}
