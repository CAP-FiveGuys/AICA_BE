package com.aica.aivoca.auth.service;

import com.aica.aivoca.auth.repository.EmailCodeRedisRepository;
import com.aica.aivoca.global.exception.BusinessException;
import com.aica.aivoca.global.exception.message.ErrorMessage;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Duration;

@RequiredArgsConstructor
@Service
public class EmailAuthService {

    private final EmailCodeRedisRepository redisRepository;
    private final JavaMailSender mailSender;

    public void sendEmailCode(String email) {
        String code = generateCode();
        redisRepository.saveCode(email, code, Duration.ofMinutes(3));
        sendEmail(email, code);
    }

    public void verifyEmailCode(String email, String inputCode) {
        String savedCode = redisRepository.getCode(email);
        if (savedCode == null || !savedCode.equals(inputCode)) {
            throw new BusinessException(ErrorMessage.INVALID_EMAIL_CODE);
        }
        redisRepository.deleteCode(email);
    }

    private String generateCode() {
        return RandomStringUtils.randomAlphanumeric(6).toUpperCase();
    }

    private void sendEmail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("AICA 이메일 인증코드");
        message.setText("인증 코드는 [" + code + "] 입니다.");
        mailSender.send(message);
    }
}
