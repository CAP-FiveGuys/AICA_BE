package com.aica.aivoca.login.controller;

import com.aica.aivoca.global.exception.dto.SuccessStatusResponse;
import com.aica.aivoca.global.exception.message.SuccessMessage;
import com.aica.aivoca.login.dto.LoginRequestDto;
import com.aica.aivoca.login.dto.LoginResponseDto;
import com.aica.aivoca.login.dto.TokenReissueRequestDto;
import com.aica.aivoca.login.service.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class LoginController {

    private final LoginService loginService;

    @PostMapping("/login")
    public ResponseEntity<SuccessStatusResponse<LoginResponseDto>> login(@RequestBody LoginRequestDto dto) {
        LoginResponseDto tokenDto = loginService.login(dto);
        return ResponseEntity.ok(SuccessStatusResponse.of(SuccessMessage.LOGIN_SUCCESS, tokenDto));
    }

    @PostMapping("/reissue")
    public ResponseEntity<SuccessStatusResponse<LoginResponseDto>> reissue(@RequestBody TokenReissueRequestDto dto) {
        LoginResponseDto response = loginService.reissue(dto);
        return ResponseEntity.ok(SuccessStatusResponse.of(SuccessMessage.TOKEN_REISSUE_SUCCESS, response));
    }

}