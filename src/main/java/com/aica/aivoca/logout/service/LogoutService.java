package com.aica.aivoca.logout.service;

import com.aica.aivoca.global.exception.BusinessException;
import com.aica.aivoca.global.exception.message.ErrorMessage;
import com.aica.aivoca.login.repository.RefreshTokenRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutService {

    private final RefreshTokenRedisRepository refreshTokenRedisRepository;

    public void logout(Long userId, String authHeader) {

        // Redis에 저장된 RefreshToken 조회
        String savedRefreshToken = refreshTokenRedisRepository.findByUserId(userId);
        if (savedRefreshToken == null) {
            throw new BusinessException(ErrorMessage.REFRESH_TOKEN_NOT_MATCH);
        }

        // RefreshToken 삭제
        refreshTokenRedisRepository.delete(userId);
    }
}