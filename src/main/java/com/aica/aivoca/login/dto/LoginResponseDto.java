package com.aica.aivoca.login.dto;

public record LoginResponseDto(
        String accessToken,
        String refreshToken
) {}
