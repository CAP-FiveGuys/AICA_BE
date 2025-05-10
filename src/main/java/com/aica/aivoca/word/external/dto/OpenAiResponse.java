package com.aica.aivoca.word.external.dto;

import java.util.List;

public record OpenAiResponse(List<Choice> choices) {
    public String getFirstMessageContent() {
        return choices.get(0).message().content();
    }

    public record Choice(Message message) {}
    public record Message(String role, String content) {}
}
