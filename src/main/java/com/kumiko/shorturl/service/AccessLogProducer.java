package com.kumiko.shorturl.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AccessLogProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public AccessLogProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(String shortCode) {
        String message = String.format("{\"shortCode\":\"%s\",\"timestamp\":\"%s\"}", shortCode, LocalDateTime.now());
        kafkaTemplate.send("url-access-log", message);
    }
}
