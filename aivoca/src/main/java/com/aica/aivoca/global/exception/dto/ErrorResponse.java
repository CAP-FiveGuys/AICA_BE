package com.aica.aivoca.global.exception.dto;

import com.aica.aivoca.global.exception.message.ErrorMessage;

public record ErrorResponse(
    int code,
    String message
) {
    public static ErrorResponse of(int code, String message) {
        return new ErrorResponse(code, message);
    }
    public static ErrorResponse of(ErrorMessage errorMessage) {
        return new ErrorResponse(errorMessage.getCode(), errorMessage.getMessage());
    }
}
