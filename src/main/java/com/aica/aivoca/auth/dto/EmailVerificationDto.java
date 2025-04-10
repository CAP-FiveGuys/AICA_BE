package com.aica.aivoca.auth.dto;

public record EmailVerificationDto(
        String email,
        String code
) {
}
