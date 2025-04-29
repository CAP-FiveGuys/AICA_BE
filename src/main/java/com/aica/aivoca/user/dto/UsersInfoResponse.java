package com.aica.aivoca.user.dto;

public record UsersInfoResponse(
        Long id,
        String userId,
        String email,
        String nickname
) {}
