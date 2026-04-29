package com.kumiko.shorturl.service;

import com.kumiko.shorturl.util.ShardRouter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class AccessLogConsumer {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public AccessLogConsumer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @KafkaListener(topics="url-access-log", groupId="shorturl-access-log")
    public void consumer(String message) {
        String shortCode = message.split("\"shortCode\":\"")[1].split("\"")[0];
        String tableName = ShardRouter.getTableName(shortCode);
        jdbcTemplate.update("UPDATE " + tableName + " SET access_count = access_count + 1 WHERE short_code = ?", shortCode);
    }
}
