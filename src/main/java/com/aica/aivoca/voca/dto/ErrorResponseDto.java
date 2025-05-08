package com.aica.aivoca.voca.dto;

public record ErrorResponseDto(
        int code,
        String message
) {}