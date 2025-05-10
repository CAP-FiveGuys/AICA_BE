package com.aica.aivoca.word.external.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public record OpenAiRequest(
        String model,
        ChatMessage[] messages
) {
    public record ChatMessage(Role role, String content) {
        public enum Role {
            USER("user"), SYSTEM("system"), ASSISTANT("assistant");

            private final String value;

            Role(String value) {
                this.value = value;
            }

            @JsonValue
            public String getValue() {
                return value;
            }
        }
    }
}
