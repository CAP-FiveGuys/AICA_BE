package com.aica.aivoca.voca.dto;


public record SuccessResponseDto<T>(
        int code,
        String message,
        T data
) {}