package io.nayoon.cs_orchestrator.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisQueue {
    private final StringRedisTemplate redis;

    public RedisQueue(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void enqueueGlobal(Long ticketId) {
        // FIFO 원하면 RPUSH + BLPOP 조합을 고려
        redis.opsForList().rightPush("queue:global", ticketId.toString());
    }
}
