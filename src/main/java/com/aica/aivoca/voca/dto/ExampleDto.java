package com.aica.aivoca.voca.dto;

/**
 * 예문 생성 성공 시 data 필드에 포함될 DTO
 * @param word 요청된 단어
 * @param example 생성된 예문
 */
public record ExampleDto(
        String word,
        String example
) {}