package com.aica.aivoca.wordinfo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiExample(
        String sentence,
        @JsonProperty("sentenceMeaning") String meaning
) {}