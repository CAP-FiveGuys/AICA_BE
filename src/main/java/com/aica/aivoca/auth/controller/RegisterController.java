package com.aica.aivoca.auth.controller;

import com.aica.aivoca.auth.dto.UserRegisterRequestDto;
import com.aica.aivoca.auth.service.RegisterService;
import com.aica.aivoca.global.exception.dto.SuccessStatusResponse;
import com.aica.aivoca.global.exception.message.SuccessMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class RegisterController {

    private final RegisterService registerService;

    @PostMapping("/register")
    public ResponseEntity<SuccessStatusResponse<Void>> register(@RequestBody UserRegisterRequestDto requestDto) {
        registerService.register(requestDto);
        return ResponseEntity.ok(SuccessStatusResponse.of(SuccessMessage.REGISTER_SUCCESS));
    }
}
