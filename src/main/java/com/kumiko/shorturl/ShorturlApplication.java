package com.kumiko.shorturl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.Instant;

@SpringBootApplication()
public class ShorturlApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShorturlApplication.class, args);
        System.out.println(Instant.parse("2026-01-01T00:00:00Z").toEpochMilli());
	}
}
