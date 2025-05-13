package com.aica.aivoca.wordinfo.external;

import com.aica.aivoca.wordinfo.dto.AiWordResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonParserUtil {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static AiWordResponse parse(String json) {
        try {
            return mapper.readValue(json, AiWordResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("AI 응답 파싱 실패: " + json, e);
        }
    }
}
