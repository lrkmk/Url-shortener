package com.kumiko.shorturl.service;

import com.kumiko.shorturl.model.UrlMapping;
import com.kumiko.shorturl.repository.UrlMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UrlShortenerService {
    private UrlMappingRepository urlMappingRepository;
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Autowired
    public UrlShortenerService(UrlMappingRepository urlMappingRepository, SnowflakeIdGenerator snowflakeIdGenerator) {
        this.urlMappingRepository = urlMappingRepository;
        this.snowflakeIdGenerator = snowflakeIdGenerator;
    }

    public String shortenUrl(String longUrl) {
        long id = snowflakeIdGenerator.nextId();
        String encodedId = Base62Encoder.encode(id);
        UrlMapping urlMapping = new UrlMapping(id, encodedId, longUrl, LocalDateTime.now(), 0);
        urlMappingRepository.save(urlMapping);
        return encodedId;
    }

    public String getLongUrlFromShortCode(String shortCode) {
        Optional<UrlMapping> result = urlMappingRepository.findByShortCode(shortCode);
        if (result.isPresent()) {
            return result.get().getLongUrl();
        } else {
            throw new RuntimeException("no url mapping with the given short code found");
        }
    }
}
