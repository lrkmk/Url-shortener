package com.kumiko.shorturl.service;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.kumiko.shorturl.repository.UrlMappingRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class BloomFilterService {
    private BloomFilter<String> bloomFilter = BloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8), 1000000, 0.01);
    @Autowired
    private UrlMappingRepository urlMappingRepository;

    @PostConstruct
    public void init() {
        List<String> shortCodes = urlMappingRepository.findAllShortCodes();
        for (String code : shortCodes) {
            bloomFilter.put(code);
        }
        System.out.println("BloomFilter preload finished, size=" + shortCodes.size());
    }

    public boolean mightContain(String shortCode) {
        return bloomFilter.mightContain(shortCode);
    }

    public void add(String shortCode) {
        bloomFilter.put(shortCode);
    }
}
