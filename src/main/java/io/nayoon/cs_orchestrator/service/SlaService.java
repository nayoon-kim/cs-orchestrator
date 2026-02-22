package io.nayoon.cs_orchestrator.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class SlaService {

    private final StringRedisTemplate redis;

    public SlaService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void registerFirstResponseSla(Long ticketId, Instant due) {
        String key = "sla:first_response";
        double score = (double)due.toEpochMilli();
        redis.opsForZSet().add(key, String.valueOf(ticketId), score);
    }
}
