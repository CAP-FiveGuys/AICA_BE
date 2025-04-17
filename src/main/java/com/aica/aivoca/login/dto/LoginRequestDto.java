package com.aica.aivoca.login.dto;

public record LoginRequestDto(
        String userId,
        String password,
        boolean rememberMe
) {}