package com.aica.aivoca.auth.service;

import com.aica.aivoca.auth.repository.EmailCodeRedisRepository;
import com.aica.aivoca.auth.repository.EmailVerificationRepository;
import com.aica.aivoca.global.exception.BusinessException;
import com.aica.aivoca.global.exception.message.ErrorMessage;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.Duration;

@RequiredArgsConstructor
@Service
public class EmailAuthService {

    private final EmailCodeRedisRepository redisRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final JavaMailSender mailSender;
    private final TemplateEngine emailTemplateEngine;

    public void sendEmailCode(String email) {
        String code = generateCode();
        redisRepository.saveCode(email, code, Duration.ofMinutes(3));
        sendHtmlEmail(email, code);
    }

    public void verifyEmailCode(String email, String inputCode) {
        String savedCode = redisRepository.getCode(email);
        if (savedCode == null || !savedCode.equals(inputCode)) {
            throw new BusinessException(ErrorMessage.INVALID_EMAIL_CODE);
        }
        redisRepository.deleteCode(email);
        emailVerificationRepository.markVerified(email);
    }

    // 6자리 랜덤 코드 생성
    private String generateCode() {
        return RandomStringUtils.randomAlphanumeric(6).toUpperCase();
    }

    // HTML 형식으로 이메일 전송 (외부 이미지 사용)
    private void sendHtmlEmail(String to, String code) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

            helper.setTo(to);
            helper.setSubject("AICA 이메일 인증코드");

            Context context = new Context();
            context.setVariable("code", code); // 이미지 변수는 더 이상 필요 없음

            String html = emailTemplateEngine.process("email/verification", context);
            helper.setText(html, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new IllegalStateException("이메일 전송 중 오류가 발생했습니다.", e);
        }
    }
}
