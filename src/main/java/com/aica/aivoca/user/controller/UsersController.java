package com.aica.aivoca.user.controller;

import com.aica.aivoca.user.dto.UsersInfoResponse;
import com.aica.aivoca.user.service.UsersService;
import com.aica.aivoca.global.jwt.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UsersController {

    private final UsersService userService;

    @GetMapping("/member")
    public UsersInfoResponse getMyInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return userService.getUserInfo(userDetails.userId());
    }
}
