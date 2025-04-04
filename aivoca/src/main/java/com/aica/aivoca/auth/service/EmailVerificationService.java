package com.aica.aivoca.auth.service;

import com.aica.aivoca.auth.repository.EmailVerificationRepository;
import com.aica.aivoca.global.exception.BusinessException;
import com.aica.aivoca.global.exception.message.ErrorMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class EmailVerificationService {

    private final EmailVerificationRepository verificationRepository;

    public void verify(String email) {
        verificationRepository.markVerified(email);
    }

    public void checkVerified(String email) {
        if (!verificationRepository.isVerified(email)) {
            throw new BusinessException(ErrorMessage.EMAIL_NOT_VERIFIED);
        }
        // 인증 완료 후엔 인증 정보 삭제(선택적)
        verificationRepository.deleteVerification(email);
    }
}
