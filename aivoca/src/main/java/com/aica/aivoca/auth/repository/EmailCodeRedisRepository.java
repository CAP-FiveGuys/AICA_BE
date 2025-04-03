package com.aica.aivoca.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@RequiredArgsConstructor
@Repository
public class EmailCodeRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public void saveCode(String email, String code, Duration ttl) {
        redisTemplate.opsForValue().set("email_auth:" + email, code, ttl);
    }

    public String getCode(String email) {
        return redisTemplate.opsForValue().get("email_auth:" + email);
    }

    public void deleteCode(String email) {
        redisTemplate.delete("email_auth:" + email);
    }
}
