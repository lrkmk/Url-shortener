package com.kumiko.shorturl.controller;

import com.kumiko.shorturl.service.UrlShortenerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("url")
public class UrlShortenerController {
    private UrlShortenerService urlShortenerService;

    @Autowired
    public UrlShortenerController(UrlShortenerService urlShortenerService){
        this.urlShortenerService = urlShortenerService;
    }

    @PostMapping("/shorten")
    public String getShortCodeFromLong(@RequestBody String longUrl){
        return urlShortenerService.shortenUrl(longUrl);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> accessFromShortCode(@PathVariable String shortCode){
        String longUrl = urlShortenerService.getLongUrlFromShortCode(shortCode);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(longUrl)).build();
    }


}
