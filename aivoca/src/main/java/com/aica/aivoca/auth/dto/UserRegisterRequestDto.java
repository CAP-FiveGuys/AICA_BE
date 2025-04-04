package com.aica.aivoca.auth.dto;

public record UserRegisterRequestDto(
        String userId,
        String email,
        String password
) {}