package com.aica.aivoca.word.dto;

import java.util.List;

public record WordGetResponseDto(
        Long wordId,
        List<Long> sentenceIds,
        String word,
        List<MeaningDto> meanings
) {}