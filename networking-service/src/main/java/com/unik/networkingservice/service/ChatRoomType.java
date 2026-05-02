package com.unik.networkingservice.service;

import java.util.Arrays;

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
}
