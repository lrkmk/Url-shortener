package com.kumiko.shorturl.service;

import com.kumiko.shorturl.repository.UrlMappingRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class UrlShortenerServiceTest {
    UrlMappingRepository urlMappingRepository = Mockito.mock(UrlMappingRepository.class);
    SnowflakeIdGenerator snowflakeIdGenerator = Mockito.mock(SnowflakeIdGenerator.class);
    UrlShortenerService urlShortenerService = new UrlShortenerService(urlMappingRepository, snowflakeIdGenerator);

    @Test
    void testShortenUrl() {
        String result = urlShortenerService.shortenUrl("https://www.google.com");
        Assertions.assertTrue(result != null && !result.isEmpty());
    }
}
