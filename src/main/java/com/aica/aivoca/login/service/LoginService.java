package com.aica.aivoca.login.service;

import com.aica.aivoca.domain.Users;
import com.aica.aivoca.global.exception.BusinessException;
import com.aica.aivoca.global.jwt.JwtTokenProvider;
import com.aica.aivoca.login.dto.LoginRequestDto;
import com.aica.aivoca.login.dto.LoginResponseDto;
import com.aica.aivoca.login.dto.TokenReissueRequestDto;
import com.aica.aivoca.login.repository.RefreshTokenRedisRepository;
import com.aica.aivoca.user.repository.UsersRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.aica.aivoca.global.exception.message.ErrorMessage;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UsersRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRedisRepository refreshTokenRedisRepository;

    /**
     * 사용자 로그인 처리 및 JWT 토큰 발급
     * @param dto 로그인 요청 DTO (userId, password, rememberMe)
     * @return accessToken, refreshToken 포함한 응답 DTO (rememberMe가 false면 refreshToken은 null)
     */
    public LoginResponseDto login(LoginRequestDto dto) {
        // 사용자 조회 (user_uid 기준)
        Users user = userRepository.findByUserId(dto.userId())
                .orElseThrow(() -> new BusinessException(ErrorMessage.USER_ID_NOT_FOUND));

        // 비밀번호 검증
        if (!passwordEncoder.matches(dto.password(), user.getPassword())) {
            throw new BusinessException(ErrorMessage.INVALID_PASSWORD);
        }

        // AccessToken 발급
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getUserId());

        // RefreshToken 발급 및 저장 (rememberMe가 true일 경우에만)
        String refreshToken = null;
        if (dto.rememberMe()) {
            refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getUserId());
            refreshTokenRedisRepository.save(user.getId(), refreshToken);
        }

        return new LoginResponseDto(accessToken, refreshToken);
    }


    /**
     * AccessToken 재발급 처리
     * @param dto 클라이언트가 보낸 RefreshToken
     * @return 새롭게 발급된 accessToken, refreshToken 응답 DTO
     */
    public LoginResponseDto reissue( TokenReissueRequestDto dto) {
        String refreshToken = dto.refreshToken();

        // RefreshToken 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorMessage.INVALID_REFRESH_TOKEN);
        }

        // RefreshToken 내부 정보 추출
        Claims claims = jwtTokenProvider.getClaims(refreshToken);
        Long userId = Long.parseLong(claims.getSubject());

        // Redis에 저장된 RefreshToken과 일치하는지 확인
        String savedRefreshToken = refreshTokenRedisRepository.findByUserId(userId);
        if (savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)) {
            throw new BusinessException(ErrorMessage.REFRESH_TOKEN_NOT_MATCH);
        }

        // 사용자 재조회 (DB에서)
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorMessage.USER_ID_NOT_FOUND));

        // 새로운 토큰 발급
        String newAccessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getUserId());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getUserId());

        // Redis에 RefreshToken 갱신
        refreshTokenRedisRepository.save(user.getId(), newRefreshToken);

        return new LoginResponseDto(newAccessToken, newRefreshToken);
    }
}
