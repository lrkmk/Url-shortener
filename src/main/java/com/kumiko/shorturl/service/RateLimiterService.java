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

    @Autowired
    public RateLimiterService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.rateLimitScript = RedisScript.of(
                "local tokens = tonumber(redis.call('get', KEYS[1])) " +
                        "if tokens == nil then tokens = tonumber(ARGV[2]) end " +
                        "if tokens > 0 then " +
                        "  redis.call('set', KEYS[1], tokens - 1, 'EX', tonumber(ARGV[1])) " +
                        "  return 1 " +
                        "else return 0 end",
                Long.class
        );
    }

    public boolean allowRequest(String ip) {
        Long result = stringRedisTemplate.execute(
                rateLimitScript,
                List.of("rate_limit:" + ip),
                "1",
                "10000"
                );
        return Long.valueOf(1L).equals(result);
    }
}
