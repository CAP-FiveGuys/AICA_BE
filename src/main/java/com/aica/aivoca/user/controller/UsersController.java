package com.aica.aivoca.user.controller;

import com.aica.aivoca.global.exception.dto.SuccessStatusResponse;
import com.aica.aivoca.global.exception.message.SuccessMessage;
import com.aica.aivoca.user.dto.UsersInfoResponse;
import com.aica.aivoca.user.service.UsersService;
import com.aica.aivoca.global.jwt.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
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

    @DeleteMapping("/member")
    public ResponseEntity<SuccessStatusResponse<Void>> deleteMyAccount(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        userService.deleteUser(userDetails.userId());
        return ResponseEntity.ok(SuccessStatusResponse.of(SuccessMessage.USER_DELETE_SUCCESS));
    }

}
