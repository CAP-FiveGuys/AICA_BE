package com.aica.aivoca.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserUpdateRequestDto(
        @Size(min = 8, message = "새 비밀번호는 최소 8자 이상이어야 합니다.")
        String newPassword, // null이면 변경 안 함, ""이면 @Size에 걸림

        String confirmNewPassword, // newPassword가 null이면 변경 안 함, ""이면 @Size에 걸림

        @Email(message = "유효한 이메일 형식이 아닙니다.")
        String newEmail, // null이면 변경 안 함, "" 또는 유효하지 않은 형식이면 @Email에 걸림

        @Size(min = 1)
        String newNickname // null이면 변경 안 함, ""이면 @Size에 걸림
) {}