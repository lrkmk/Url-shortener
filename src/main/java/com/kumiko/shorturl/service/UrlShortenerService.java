package com.kumiko.shorturl.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.kumiko.shorturl.exception.UrlNotFoundException;
import com.kumiko.shorturl.util.ShardRouter;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class UrlShortenerService {
    private final AccessLogProducer accessLogProducer;
    private SnowflakeIdGenerator snowflakeIdGenerator;
    private StringRedisTemplate stringRedisTemplate;
    private BloomFilterService bloomFilterService;
    private Cache<String, String> localCache;
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public UrlShortenerService(SnowflakeIdGenerator snowflakeIdGenerator, StringRedisTemplate stringRedisTemplate, BloomFilterService bloomFilterService, AccessLogProducer accessLogProducer, JdbcTemplate jdbcTemplate) {
        this.snowflakeIdGenerator = snowflakeIdGenerator;
        this.stringRedisTemplate = stringRedisTemplate;
        this.bloomFilterService = bloomFilterService;
        this.accessLogProducer = accessLogProducer;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    void initCache() {
        localCache = Caffeine.newBuilder().maximumSize(10000).expireAfterWrite(5, TimeUnit.MINUTES).build();
    }

    public String shortenUrl(String longUrl) {
        long id = snowflakeIdGenerator.nextId();
        String encodedId = Base62Encoder.encode(id);

        String tableName = ShardRouter.getTableName(encodedId);
        jdbcTemplate.update(
                "INSERT INTO " + tableName + " (id, short_code, long_url, created_at, access_count) VALUES (?, ?, ?, ?, ?)",
                id, encodedId, longUrl, LocalDateTime.now(), 0
        );

        bloomFilterService.add(encodedId);
        localCache.put(encodedId, longUrl);
        stringRedisTemplate.opsForValue().set(encodedId, longUrl, 24, TimeUnit.HOURS);

        return encodedId;
    }

    public String getLongUrlFromShortCode(String shortCode) {
        if (!bloomFilterService.mightContain(shortCode)) {
            throw new UrlNotFoundException("no url mapping with the given short code found");
        }

        String value = localCache.getIfPresent(shortCode);
        if (value != null) {
            accessLogProducer.send(shortCode);
            return value;
        }

        String cached = stringRedisTemplate.opsForValue().get(shortCode);
        if (cached != null) {
            localCache.put(shortCode, cached);
            accessLogProducer.send(shortCode);
            return cached;
        }

        String tableName = ShardRouter.getTableName(shortCode);
        List<String> results = jdbcTemplate.query(
                "SELECT long_url FROM " + tableName + " WHERE short_code = ?",
                (rs, rowNum) -> rs.getString("long_url"),
                shortCode
        );

        if (!results.isEmpty()) {
            String longUrl = results.get(0);
            stringRedisTemplate.opsForValue().set(shortCode, longUrl, 24, TimeUnit.HOURS);
            localCache.put(shortCode, longUrl);
            accessLogProducer.send(shortCode);
            return longUrl;
        } else {
            throw new UrlNotFoundException("no url mapping with the given short code found");
        }
    }

    public void deleteUrl(String shortCode) {
        localCache.invalidate(shortCode);
        stringRedisTemplate.delete(shortCode);

        String tableName = ShardRouter.getTableName(shortCode);
        jdbcTemplate.update("DELETE FROM " + tableName + " WHERE short_code = ?", shortCode);

        CompletableFuture.runAsync(() -> {
            localCache.invalidate(shortCode);
            stringRedisTemplate.delete(shortCode);
        }, CompletableFuture.delayedExecutor(500, TimeUnit.MILLISECONDS));
    }

    public long getAccessCount(String shortCode) {
        if (!bloomFilterService.mightContain(shortCode)) {
            throw new UrlNotFoundException("no url with given short code exists");
        }

        String tableName = ShardRouter.getTableName(shortCode);
        List<Long> count = jdbcTemplate.query("SELECT access_count FROM " + tableName + " WHERE short_code = ?", (rs, rownum) -> rs.getLong("access_count"), shortCode);
        if (count.isEmpty()) {
            throw new UrlNotFoundException("no access count for the given short code");
        }
        return count.get(0);
    }
}
