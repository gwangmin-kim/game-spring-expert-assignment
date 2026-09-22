package com.gameexpert.chat.service;

import java.util.List;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRateLimitService {
    private static final String COUNT_LIMIT = "5";
    private static final String TTL = "10";

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<Long> incrementChatCountScript;

    public boolean allow(Long playerId) {
        // Lv 19: Lua 스크립트를 사용해 횟수 확인부터 최초 만료 설정까지 원자적으로 실행
        String key = "chat:limit:" + playerId;
        Long updated = redisTemplate.execute(incrementChatCountScript,
                List.of(key), COUNT_LIMIT, TTL);
        return updated == 1L;
    }
}
