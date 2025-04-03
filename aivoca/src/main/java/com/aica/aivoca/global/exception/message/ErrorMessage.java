package com.aica.aivoca.global.exception.message;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorMessage {
    INVALID_EMAIL_CODE(HttpStatus.UNAUTHORIZED.value(), "인증 코드가 일치하지 않거나 만료되었습니다."),;


    private final int code;
    private final String message;
}
