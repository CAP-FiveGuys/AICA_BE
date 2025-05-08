package com.aica.aivoca.voca.service;

public interface ExampleGenerationService {
    /**
     * 주어진 단어에 대한 예문을 생성합니다.
     * @param word 예문을 생성할 단어
     * @return 생성된 예문 문자열
     * @throws RuntimeException AI 서비스 호출 실패 등 예외 발생 시
     */
    String generateExample(String word);
}