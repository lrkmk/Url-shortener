package com.kumiko.shorturl.service;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class BloomFilterService {
    private BloomFilter<String> bloomFilter = BloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8), 1000000, 0.01);
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public BloomFilterService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        List<String> shortCodes = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            shortCodes.addAll(jdbcTemplate.query(
                    "SELECT short_code FROM url_mapping_" + i,
                    (rs, rowNum) -> rs.getString("short_code")
            ));
        }
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
