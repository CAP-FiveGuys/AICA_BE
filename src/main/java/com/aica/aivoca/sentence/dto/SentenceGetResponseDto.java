package com.aica.aivoca.sentence.dto;

public record SentenceGetResponseDto(
        Long sentenceId,
        Long userId,
        String sentence
) {}
