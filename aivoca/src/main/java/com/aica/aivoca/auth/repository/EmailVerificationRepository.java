package com.aica.aivoca.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@RequiredArgsConstructor
@Repository
public class EmailVerificationRepository {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String PREFIX = "email_verified:";

    public void markVerified(String email) {
        redisTemplate.opsForValue().set(PREFIX + email, "true", Duration.ofMinutes(10)); // 10분 유지
    }

    public boolean isVerified(String email) {
        return "true".equals(redisTemplate.opsForValue().get(PREFIX + email));
    }

    public void deleteVerification(String email) {
        redisTemplate.delete(PREFIX + email);
    }
}
