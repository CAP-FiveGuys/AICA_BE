package com.aica.aivoca.user.controller;

import com.aica.aivoca.global.exception.dto.SuccessStatusResponse;
import com.aica.aivoca.global.exception.message.SuccessMessage;
import com.aica.aivoca.user.dto.UserUpdateRequestDto;
import com.aica.aivoca.user.dto.UsersInfoResponse;
import com.aica.aivoca.user.service.UsersService;
import com.aica.aivoca.global.jwt.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    @PatchMapping("/member")
    public ResponseEntity<SuccessStatusResponse<Map<String, String>>> updateUserInfo(

            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid UserUpdateRequestDto requestDto) {

        Map<String, String> updatedInfo = userService.updateUser(userDetails.userId(), requestDto);

        return ResponseEntity.ok(
                SuccessStatusResponse.of(SuccessMessage.MEMBER_UPDATE_SUCCESS, updatedInfo)
        );
    }
}