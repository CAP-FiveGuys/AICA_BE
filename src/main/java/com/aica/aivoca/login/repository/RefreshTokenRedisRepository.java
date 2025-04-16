package com.aica.aivoca.login.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public void save(Long userId, String refreshToken) {
        String key = getKey(userId);
        redisTemplate.opsForValue().set(key, refreshToken, 14, TimeUnit.DAYS); // TTL 설정
    }

    public String findByUserId(Long userId) {
        return redisTemplate.opsForValue().get(getKey(userId));
    }

    public void delete(Long userId) {
        redisTemplate.delete(getKey(userId));
    }

    private String getKey(Long userId) {
        return "refresh:" + userId;
    }
}