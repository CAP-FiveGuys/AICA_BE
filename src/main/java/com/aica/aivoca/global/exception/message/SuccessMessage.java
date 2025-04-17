package com.aica.aivoca.global.exception.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum SuccessMessage {

    EMAIL_REQUEST_SUCCESS(HttpStatus.CREATED.value(), "이메일 인증코드가 전송되었습니다."),
    EMAIL_VERIFY_SUCCESS(HttpStatus.OK.value(), "이메일 인증이 완료되었습니다."),
    REGISTER_SUCCESS(HttpStatus.CREATED.value(), "회원가입에 성공하였습니다."),

    // 문장 관련

    SENTENCE_ADD_SUCCESS(HttpStatus.CREATED.value(), "문장이 성공적으로 추가되었습니다."),
    SENTENCE_GET_SUCCESS(HttpStatus.OK.value(), "문장 목록이 성공적으로 조회되었습니다."),

    // 단어 캐싱
    WORD_CACHED_SUCCESS(HttpStatus.CREATED.value(), "단어가 캐싱 테이블에 성공적으로 추가되었습니다."),

    // 단어장 추가
    WORD_ADDED_TO_VOCABULARY(HttpStatus.OK.value(), "단어가 단어장에 성공적으로 추가되었습니다."),

    // 단어장 조회
    GET_WORD_SUCCESS(HttpStatus.OK.value(), "단어장을 성공적으로 조회했습니다."),

    //로그인
    LOGIN_SUCCESS(HttpStatus.OK.value(), "로그인에 성공하였습니다."),
    TOKEN_REISSUE_SUCCESS(HttpStatus.CREATED.value(), "토큰이 재발행 되었습니다."),

    ;


    private final int code;
    private final String message;
}
