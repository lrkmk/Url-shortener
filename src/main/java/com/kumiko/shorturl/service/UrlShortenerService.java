package com.kumiko.shorturl.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.kumiko.shorturl.model.UrlMapping;
import com.kumiko.shorturl.repository.UrlMappingRepository;
import jakarta.annotation.PostConstruct;
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
    private BloomFilterService bloomFilterService;
    private Cache<String, String> localCache;

    @Autowired
    public UrlShortenerService(UrlMappingRepository urlMappingRepository, SnowflakeIdGenerator snowflakeIdGenerator, StringRedisTemplate stringRedisTemplate, BloomFilterService bloomFilterService) {
        this.urlMappingRepository = urlMappingRepository;
        this.snowflakeIdGenerator = snowflakeIdGenerator;
        this.stringRedisTemplate = stringRedisTemplate;
        this.bloomFilterService = bloomFilterService;

    }

    @PostConstruct
    private void initCache() {
        localCache = Caffeine.newBuilder().maximumSize(10000).expireAfterWrite(5, TimeUnit.MINUTES).build();
    }

    public String shortenUrl(String longUrl) {
        long id = snowflakeIdGenerator.nextId();
        String encodedId = Base62Encoder.encode(id);

        UrlMapping urlMapping = new UrlMapping(id, encodedId, longUrl, LocalDateTime.now(), 0);
        urlMappingRepository.save(urlMapping);

        bloomFilterService.add(encodedId);
        localCache.put(encodedId, longUrl);
        stringRedisTemplate.opsForValue().set(encodedId, longUrl, 24, TimeUnit.HOURS);

        return encodedId;
    }

    public String getLongUrlFromShortCode(String shortCode) {
        if (!bloomFilterService.mightContain(shortCode)) {
            throw new RuntimeException("no url mapping with the given short code found");
        }

        String value = localCache.getIfPresent(shortCode);
        if (value != null) {
            return value;
        }

        String cached = stringRedisTemplate.opsForValue().get(shortCode);
        if (cached != null) {
            localCache.put(shortCode, cached);
            return cached;
        }

        Optional<UrlMapping> result = urlMappingRepository.findByShortCode(shortCode);
        if (result.isPresent()) {
            stringRedisTemplate.opsForValue().set(shortCode, result.get().getLongUrl(), 24, TimeUnit.HOURS);
            localCache.put(shortCode,result.get().getLongUrl());
            return result.get().getLongUrl();
        } else {
            throw new RuntimeException("no url mapping with the given short code found");
        }
    }
}
