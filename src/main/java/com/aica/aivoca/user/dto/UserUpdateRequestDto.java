package com.aica.aivoca.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@JsonInclude(JsonInclude.Include.NON_NULL) // NULL 값 필드는 응답에 포함하지 않음
public record UserUpdateRequestDto(
        String currentPassword, // 현재 비밀번호 (비밀번호 변경 시 필수)
        @Size(min = 8, message = "새 비밀번호는 최소 8자 이상이어야 합니다.")
        String newPassword,     // 새로운 비밀번호 (비밀번호 변경 시 필수)
        String confirmNewPassword, // 새로운 비밀번호 확인 (비밀번호 변경 시 필수)

        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String newEmail         // 새로운 이메일 (이메일 변경 시 필수)
) {}