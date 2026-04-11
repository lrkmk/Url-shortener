package com.kumiko.shorturl.service;

import com.kumiko.shorturl.model.UrlMapping;
import com.kumiko.shorturl.repository.UrlMappingRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


public class UrlShortenerServiceTest {
    UrlMappingRepository urlMappingRepository = mock(UrlMappingRepository.class);
    SnowflakeIdGenerator snowflakeIdGenerator = mock(SnowflakeIdGenerator.class);
    StringRedisTemplate stringRedisTemplate = mock(StringRedisTemplate.class);
    ValueOperations<String, String> valueOps = mock(ValueOperations.class);
    UrlShortenerService urlShortenerService = new UrlShortenerService(urlMappingRepository, snowflakeIdGenerator, stringRedisTemplate);

    @Test
    void testShortenUrl() {
        String longUrl = "https://example.com";
        when(snowflakeIdGenerator.nextId()).thenReturn(12345L);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
        String expectedShortCode = Base62Encoder.encode(12345L);

        String result = urlShortenerService.shortenUrl(longUrl);

        assertEquals(expectedShortCode, result);
        verify(urlMappingRepository, times(1)).save(any(UrlMapping.class));
        verify(valueOps).set(eq(expectedShortCode), eq(longUrl),
                eq(24L),
                eq(TimeUnit.HOURS));
    }

    @Test
    void testGetLongUrlFromShortCode_found() {
        String expectedLongUrl = "https://example.com";
        String shortCode = "abc123";
        UrlMapping urlMapping = new UrlMapping();
        urlMapping.setLongUrl(expectedLongUrl);
        urlMapping.setShortCode(shortCode);
        when(urlMappingRepository.findByShortCode(shortCode)).thenReturn(Optional.of(urlMapping));
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(shortCode)).thenReturn(null);

        Assertions.assertEquals(expectedLongUrl, urlShortenerService.getLongUrlFromShortCode(shortCode));
        verify(valueOps).set(eq(shortCode), eq(expectedLongUrl), eq(24L), eq(TimeUnit.HOURS));
    }

    @Test
    void testGetLongUrlFromShortCode_notFound() {
        String shortCode = "xyz";
        when(urlMappingRepository.findByShortCode(shortCode)).thenReturn(Optional.empty());
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);

        Assertions.assertThrows(RuntimeException.class, () -> urlShortenerService.getLongUrlFromShortCode(shortCode));
    }

    @Test
    void testGetLongUrlFromShortCode_cacheHit() {
        String expectedLongUrl = "https://example.com";
        String shortCode = "abc123";
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(shortCode)).thenReturn(expectedLongUrl);

        String longUrl = urlShortenerService.getLongUrlFromShortCode(shortCode);

        verify(urlMappingRepository, never()).findByShortCode(shortCode);
        Assertions.assertEquals(expectedLongUrl, longUrl);
    }

}
