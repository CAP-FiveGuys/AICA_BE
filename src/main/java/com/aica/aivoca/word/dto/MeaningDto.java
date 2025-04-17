package com.aica.aivoca.word.dto;

import java.util.List;

public record MeaningDto(
        String meaning,
        List<String> partOfSpeech,
        List<ExampleSentenceDto> exampleSentences
) {}