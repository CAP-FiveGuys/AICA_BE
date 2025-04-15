package com.aica.aivoca.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 토큰을 생성하고, 유효성 검증 및 Claims 정보를 추출하는 유틸리티 클래스
 */
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    // .env 에서 주입받는 값들
    @Value("${JWT_SECRET_KEY}")
    private String secretKey;

    @Value("${JWT_ACCESS_EXPIRATION}")  // access 토큰 만료 시간 (ms)
    private long accessTokenValidity;

    @Value("${JWT_REFRESH_EXPIRATION}") // refresh 토큰 만료 시간 (ms)
    private long refreshTokenValidity;

    // 사용할 서명 알고리즘 (HMAC SHA-256)
    private final SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS256;

    /**
     * AccessToken 생성 메서드
     */
    public String createAccessToken(Long userId, String userUid) {
        return createToken(userId, userUid, accessTokenValidity);
    }

    /**
     * RefreshToken 생성 메서드
     */
    public String createRefreshToken(Long userId, String userUid) {
        return createToken(userId, userUid, refreshTokenValidity);
    }


    private String createToken(Long userId, String userUid, long validity) {
        Claims claims = Jwts.claims().setSubject(String.valueOf(userId));
        claims.put("user_uid", userUid);

        Date now = new Date();
        Date expiry = new Date(now.getTime() + validity);

        // JWT 생성
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(
                        Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)),
                        SIGNATURE_ALGORITHM
                )
                .compact();
    }

    //토큰 유효성
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    //토큰 받기
    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    //토큰 필터
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaims(token);
        return Long.parseLong(claims.getSubject()); // subject에 userId가 들어 있음
    }
}

