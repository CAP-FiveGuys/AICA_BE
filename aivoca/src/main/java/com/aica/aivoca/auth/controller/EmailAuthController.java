package com.aica.aivoca.auth.controller;

import com.aica.aivoca.auth.dto.EmailRequestDto;
import com.aica.aivoca.auth.dto.EmailVerificationDto;
import com.aica.aivoca.auth.service.EmailAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth/email")
public class EmailAuthController {

    private final EmailAuthService emailAuthService;

    @PostMapping("/request")
    public ResponseEntity<String> sendEmailCode(@RequestBody EmailRequestDto dto) {
        emailAuthService.sendEmailCode(dto.email());
        return ResponseEntity.ok("이메일 인증코드가 전송되었습니다.");
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyEmailCode(@RequestBody EmailVerificationDto dto) {
        emailAuthService.verifyEmailCode(dto.email(), dto.code());
        return ResponseEntity.ok("이메일 인증이 완료되었습니다.");
    }
}