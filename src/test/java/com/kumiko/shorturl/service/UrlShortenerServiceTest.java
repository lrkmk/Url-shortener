package com.kumiko.shorturl.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class UrlShortenerServiceTest {
    SnowflakeIdGenerator snowflakeIdGenerator = mock(SnowflakeIdGenerator.class);
    StringRedisTemplate stringRedisTemplate = mock(StringRedisTemplate.class);
    ValueOperations<String, String> valueOps = mock(ValueOperations.class);
    BloomFilterService bloomFilterService = mock(BloomFilterService.class);
    AccessLogProducer accessLogProducer = mock(AccessLogProducer.class);
    JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    UrlShortenerService urlShortenerService = new UrlShortenerService(snowflakeIdGenerator, stringRedisTemplate, bloomFilterService, accessLogProducer, jdbcTemplate);

    @BeforeEach
    void setUp() {
        urlShortenerService.initCache();
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void testShortenUrl() {
        String longUrl = "https://example.com";
        when(snowflakeIdGenerator.nextId()).thenReturn(12345L);
        String expectedShortCode = Base62Encoder.encode(12345L);

        String result = urlShortenerService.shortenUrl(longUrl);

        assertEquals(expectedShortCode, result);
        verify(jdbcTemplate, times(1)).update(
                ArgumentMatchers.contains("INSERT INTO"),
                eq(12345L), eq(expectedShortCode), eq(longUrl),
                any(), eq(0)
        );
        verify(valueOps).set(eq(expectedShortCode), eq(longUrl), eq(24L), eq(TimeUnit.HOURS));
    }

    @Test
    void testGetLongUrlFromShortCode_found() {
        String expectedLongUrl = "https://example.com";
        String shortCode = "abc123";
        when(bloomFilterService.mightContain(shortCode)).thenReturn(true);
        when(valueOps.get(shortCode)).thenReturn(null);
        when(jdbcTemplate.query(anyString(), ArgumentMatchers.<RowMapper<String>>any(), eq(shortCode)))
                .thenReturn(List.of(expectedLongUrl));

        Assertions.assertEquals(expectedLongUrl, urlShortenerService.getLongUrlFromShortCode(shortCode));
        verify(valueOps).set(eq(shortCode), eq(expectedLongUrl), eq(24L), eq(TimeUnit.HOURS));
    }

    @Test
    void testGetLongUrlFromShortCode_notFound() {
        String shortCode = "xyz";
        when(bloomFilterService.mightContain(shortCode)).thenReturn(false);

        Assertions.assertThrows(RuntimeException.class, () -> urlShortenerService.getLongUrlFromShortCode(shortCode));
    }

    @Test
    void testGetLongUrlFromShortCode_cacheHit() {
        String expectedLongUrl = "https://example.com";
        String shortCode = "abc123";
        when(bloomFilterService.mightContain(shortCode)).thenReturn(true);
        when(valueOps.get(shortCode)).thenReturn(expectedLongUrl);

        String longUrl = urlShortenerService.getLongUrlFromShortCode(shortCode);

        verify(jdbcTemplate, never()).query(anyString(), ArgumentMatchers.<RowMapper<String>>any(), any());
        Assertions.assertEquals(expectedLongUrl, longUrl);
    }
}
