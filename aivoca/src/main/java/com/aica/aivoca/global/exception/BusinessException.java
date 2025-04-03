package com.aica.aivoca.global.exception;

import com.aica.aivoca.global.exception.message.ErrorMessage;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final ErrorMessage errorMessage;
    public BusinessException(ErrorMessage errorMessage) {
        super(errorMessage.getMessage());
        this.errorMessage = errorMessage;
    }
}