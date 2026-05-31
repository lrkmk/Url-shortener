package com.kumiko.shorturl.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RateLimiterService {
    private final StringRedisTemplate stringRedisTemplate;
    private final RedisScript<Long> rateLimitScript;

    private static final int RATE = 20;      // tokens refilled per second
    private static final int CAPACITY = 20;  // max bucket size

    @Autowired
    public RateLimiterService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.rateLimitScript = RedisScript.of(
                "local rate = tonumber(ARGV[1]) " +
                "local capacity = tonumber(ARGV[2]) " +
                "local now = tonumber(ARGV[3]) " +
                "local tokens = tonumber(redis.call('HGET', KEYS[1], 'tokens')) " +
                "local last_refill = tonumber(redis.call('HGET', KEYS[1], 'last_refill')) " +
                "if tokens == nil then tokens = capacity last_refill = now end " +
                "local elapsed = math.max(0, now - last_refill) " +
                "local refill = math.floor(elapsed * rate) " +
                "if refill > 0 then " +
                "  tokens = math.min(capacity, tokens + refill) " +
                "  last_refill = now " +
                "end " +
                "local allowed = 0 " +
                "if tokens >= 1 then tokens = tokens - 1 allowed = 1 end " +
                "local ttl = math.ceil(capacity / rate) * 2 " +
                "redis.call('HMSET', KEYS[1], 'tokens', tokens, 'last_refill', last_refill) " +
                "redis.call('EXPIRE', KEYS[1], ttl) " +
                "return allowed",
                Long.class
        );
    }

    public boolean allowRequest(String ip) {
        long now = System.currentTimeMillis() / 1000;
        Long result = stringRedisTemplate.execute(
                rateLimitScript,
                List.of("rate_limit:" + ip),
                String.valueOf(RATE),
                String.valueOf(CAPACITY),
                String.valueOf(now)
        );
        return Long.valueOf(1L).equals(result);
    }
}
