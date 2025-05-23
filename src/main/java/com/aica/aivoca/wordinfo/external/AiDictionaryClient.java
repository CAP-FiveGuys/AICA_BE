package com.aica.aivoca.wordinfo.external;

import com.aica.aivoca.wordinfo.dto.AiWordResponse;

public interface AiDictionaryClient {
    AiWordResponse getWordInfo(String word);
}
