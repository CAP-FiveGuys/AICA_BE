package com.aica.aivoca.user.dto;

import jakarta.validation.constraints.NotBlank;

public record PasswordVerificationRequestDto(
        @NotBlank(message = "현재 비밀번호는 비어 있을 수 없습니다.")
        String currentPassword
) {}