package com.aica.aivoca.global.exception.message;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorMessage {
    INVALID_EMAIL_CODE(HttpStatus.UNAUTHORIZED.value(), "인증 코드가 일치하지 않거나 만료되었습니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.UNAUTHORIZED.value(),"이메일 인증이 되지 않았습니다."),
    ALREADY_EXISTS_USER(HttpStatus.UNAUTHORIZED.value(),"이미 가입된 유저입니다."),
    DUPLICATED_USER_ID(HttpStatus.UNAUTHORIZED.value(),"이미 사용 중인 아이디입니다."),
    DUPLICATED_EMAIL(HttpStatus.UNAUTHORIZED.value(),"이미 사용 중인 이메일입니다."),
    ;

    private final int code;
    private final String message;
}
