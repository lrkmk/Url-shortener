package com.kumiko.shorturl.service;

import com.kumiko.shorturl.model.UrlMapping;
import com.kumiko.shorturl.repository.UrlMappingRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


public class UrlShortenerServiceTest {
    UrlMappingRepository urlMappingRepository = mock(UrlMappingRepository.class);
    SnowflakeIdGenerator snowflakeIdGenerator = mock(SnowflakeIdGenerator.class);
    UrlShortenerService urlShortenerService = new UrlShortenerService(urlMappingRepository, snowflakeIdGenerator);

    @Test
    void testShortenUrl() {
        String longUrl = "https://example.com";
        when(snowflakeIdGenerator.nextId()).thenReturn(12345L);

        String expectedShortCode = Base62Encoder.encode(12345L);

        String result = urlShortenerService.shortenUrl(longUrl);

        assertEquals(expectedShortCode, result);
        verify(urlMappingRepository, times(1)).save(any(UrlMapping.class));
    }

    @Test
    void testGetLongUrlFromShortCode_found() {
        String expectedLongUrl = "https://example.com";
        String shortCode = "abc123";
        UrlMapping urlMapping = new UrlMapping();
        urlMapping.setLongUrl(expectedLongUrl);
        urlMapping.setShortCode(shortCode);
        when(urlMappingRepository.findByShortCode(shortCode)).thenReturn(Optional.of(urlMapping));

        Assertions.assertEquals(expectedLongUrl, urlShortenerService.getLongUrlFromShortCode(shortCode));
    }

    @Test
    void testGetLongUrlFromShortCode_notFound() {
        String shortCode = "xyz";
        when(urlMappingRepository.findByShortCode(shortCode)).thenReturn(Optional.empty());

        Assertions.assertThrows(RuntimeException.class, () -> urlShortenerService.getLongUrlFromShortCode(shortCode));
    }
}
