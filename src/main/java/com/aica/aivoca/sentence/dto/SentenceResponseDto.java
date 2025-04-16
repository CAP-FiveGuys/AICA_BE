package com.aica.aivoca.sentence.dto;

public record SentenceResponseDto(
        Long sentenceId,
        Long userid,
        String sentence) {}