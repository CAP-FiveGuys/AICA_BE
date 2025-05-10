package com.aica.aivoca.word.external;

import com.aica.aivoca.word.external.dto.AiWordResponse;

public interface AiDictionaryClient {
    AiWordResponse getWordInfo(String word);
}
