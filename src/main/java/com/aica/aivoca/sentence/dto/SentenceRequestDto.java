package com.aica.aivoca.sentence.dto;

public record SentenceRequestDto(
        Long sentenceId,
        Long userId,
        String sentence
) {}