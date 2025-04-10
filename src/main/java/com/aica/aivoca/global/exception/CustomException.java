package com.aica.aivoca.global.exception;

import com.aica.aivoca.global.exception.message.ErrorMessage;

public class CustomException extends BusinessException {
    public CustomException(ErrorMessage errorMessage) {
        super(errorMessage);
    }
}
