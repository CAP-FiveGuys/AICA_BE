package com.aica.aivoca.wordinfo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record AiMeaning(
        String meaning,
        @JsonProperty("partsOfSpeech") List<String> partOfSpeech,
        @JsonProperty("exampleSentences") List<AiExample> exampleSentences
) {}