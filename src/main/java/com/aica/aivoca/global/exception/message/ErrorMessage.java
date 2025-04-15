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

    // 문장 관련
    INVALID_REQUEST(HttpStatus.BAD_REQUEST.value(), "잘못된 요청입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "사용자를 찾을 수 없습니다."),
    SENTENCE_ALREADY_EXISTS(HttpStatus.CONFLICT.value(), "이미 등록된 문장입니다."),
    SENTENCE_ID_ALREADY_EXISTS(HttpStatus.CONFLICT.value(), "이미 사용 중인 문장 ID입니다."),
    USER_ID_REQUIRED(HttpStatus.BAD_REQUEST.value(), "사용자 ID가 필요합니다."),
    SENTENCE_ID_REQUIRED(HttpStatus.BAD_REQUEST.value(), "문장 ID가 필요합니다."),
    SENTENCE_TEXT_REQUIRED(HttpStatus.BAD_REQUEST.value(), "문장 내용이 필요합니다."),

    // 서버 오류
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버에 알 수 없는 오류가 발생했습니다."),

    // 단어 관련
    WORD_ID_REQUIRED(HttpStatus.BAD_REQUEST.value(), "단어 ID가 필요합니다."),
    WORD_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "해당 단어를 찾을 수 없습니다."),
    WORD_ALREADY_IN_VOCABULARY(HttpStatus.CONFLICT.value(), "이미 단어장에 존재하는 단어입니다.");

    private final int code;
    private final String message;
}
