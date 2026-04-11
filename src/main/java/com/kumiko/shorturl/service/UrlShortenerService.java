package com.kumiko.shorturl.service;

import com.kumiko.shorturl.model.UrlMapping;
import com.kumiko.shorturl.repository.UrlMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class UrlShortenerService {
    private UrlMappingRepository urlMappingRepository;
    private SnowflakeIdGenerator snowflakeIdGenerator;
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    public UrlShortenerService(UrlMappingRepository urlMappingRepository, SnowflakeIdGenerator snowflakeIdGenerator, StringRedisTemplate stringRedisTemplate) {
        this.urlMappingRepository = urlMappingRepository;
        this.snowflakeIdGenerator = snowflakeIdGenerator;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public String shortenUrl(String longUrl) {
        long id = snowflakeIdGenerator.nextId();
        String encodedId = Base62Encoder.encode(id);
        UrlMapping urlMapping = new UrlMapping(id, encodedId, longUrl, LocalDateTime.now(), 0);
        urlMappingRepository.save(urlMapping);
        stringRedisTemplate.opsForValue().set(encodedId, longUrl, 24, TimeUnit.HOURS);
        return encodedId;
    }

    public String getLongUrlFromShortCode(String shortCode) {
        String cached = stringRedisTemplate.opsForValue().get(shortCode);
        if (cached != null) {
            return cached;
        }

        Optional<UrlMapping> result = urlMappingRepository.findByShortCode(shortCode);
        if (result.isPresent()) {
            stringRedisTemplate.opsForValue().set(shortCode, result.get().getLongUrl(), 24, TimeUnit.HOURS);
            return result.get().getLongUrl();
        } else {
            throw new RuntimeException("no url mapping with the given short code found");
        }
    }
}
