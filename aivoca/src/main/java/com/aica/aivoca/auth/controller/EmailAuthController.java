package com.aica.aivoca.auth.controller;

import com.aica.aivoca.auth.dto.EmailRequestDto;
import com.aica.aivoca.auth.dto.EmailVerificationDto;
import com.aica.aivoca.auth.service.EmailAuthService;
import com.aica.aivoca.global.exception.dto.SuccessStatusResponse;
import com.aica.aivoca.global.exception.message.SuccessMessage;
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
    public ResponseEntity<SuccessStatusResponse<Void>> sendEmailCode(@RequestBody EmailRequestDto dto) {
        emailAuthService.sendEmailCode(dto.email());
        return ResponseEntity.ok(SuccessStatusResponse.of(SuccessMessage.EMAIL_REQUEST_SUCCESS));
    }

    @PostMapping("/verify")
    public ResponseEntity<SuccessStatusResponse<Void>> verifyEmailCode(@RequestBody EmailVerificationDto dto) {
        emailAuthService.verifyEmailCode(dto.email(), dto.code());
        return ResponseEntity.ok(SuccessStatusResponse.of(SuccessMessage.EMAIL_VERIFY_SUCCESS));
    }
}
