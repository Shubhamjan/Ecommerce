package com.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class TokenBlacklistService {

    @Autowired
    private ReactiveRedisTemplate<String, String> redisTemplate;

    public Mono<Boolean> isBlacklisted(String token) {
        return redisTemplate.hasKey(token);
    }
}
