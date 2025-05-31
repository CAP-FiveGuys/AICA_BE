package com.aica.aivoca.auth.service;

import com.aica.aivoca.auth.repository.EmailCodeRedisRepository;
import com.aica.aivoca.auth.repository.EmailVerificationRepository;
import com.aica.aivoca.global.exception.BusinessException;
import com.aica.aivoca.global.exception.message.ErrorMessage;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.Duration;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmailAuthService {

    private final EmailCodeRedisRepository redisRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final JavaMailSender mailSender;
    private final TemplateEngine emailTemplateEngine;

    public void sendEmailCode(String email) {
        String code = generateCode();
        log.info("📨 이메일 인증 코드 생성: email={}, code={}", email, code);
        try {
            redisRepository.saveCode(email, code, Duration.ofMinutes(3));
            log.info("✅ Redis 저장 성공: email={}, TTL=3분", email);
        } catch (Exception e) {
            log.error("❌ Redis 저장 실패: email={}, error={}", email, e.getMessage());
            throw new BusinessException(ErrorMessage.INTERNAL_SERVER_ERROR);
        }
        sendHtmlEmail(email, code);
    }

    public void verifyEmailCode(String email, String inputCode) {
        String savedCode = redisRepository.getCode(email);
        log.info("🔍 이메일 인증 코드 검증 요청: email={}, 입력 코드={}, 저장 코드={}", email, inputCode, savedCode);
        if (savedCode == null || !savedCode.equals(inputCode)) {
            log.warn("❌ 이메일 인증 코드 불일치: email={}, 입력={}, 저장={}", email, inputCode, savedCode);
            throw new BusinessException(ErrorMessage.INVALID_EMAIL_CODE);
        }
        redisRepository.deleteCode(email);
        emailVerificationRepository.markVerified(email);
        log.info("✅ 이메일 인증 완료: email={}", email);
    }

    private String generateCode() {
        return RandomStringUtils.randomAlphanumeric(6).toUpperCase();
    }

    private void sendHtmlEmail(String to, String code) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

            helper.setTo(to);
            helper.setSubject("AICA 이메일 인증코드");

            Context context = new Context();
            context.setVariable("code", code);

            String html = emailTemplateEngine.process("email/verification", context);
            helper.setText(html, true);

            mailSender.send(mimeMessage);
            log.info("✅ 이메일 전송 성공: email={}", to);
        } catch (MessagingException e) {
            log.error("❌ 이메일 전송 실패: email={}, error={}", to, e.getMessage());
            throw new BusinessException(ErrorMessage.EMAIL_SEND_ERROR);
        }
    }
}
