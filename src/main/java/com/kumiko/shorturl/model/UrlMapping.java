package com.kumiko.shorturl.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "url_mapping")
public class UrlMapping {
    @Id
    private long id;
    @Column(name = "short_code")
    private String shortCode;
    @Column(name = "long_url", columnDefinition = "TEXT")
    private String longUrl;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "access_count")
    private int accessCount;

    public UrlMapping() {}

    public UrlMapping(long id, String shortCode, String longUrl, LocalDateTime createdAt, int accessCount) {
        this.id = id;
        this.shortCode = shortCode;
        this.longUrl = longUrl;
        this.createdAt = createdAt;
        this.accessCount = accessCount;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }

    public String getLongUrl() { return longUrl; }
    public void setLongUrl(String longUrl) { this.longUrl = longUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public int getAccessCount() { return accessCount; }
    public void setAccessCount(int accessCount) { this.accessCount = accessCount; }
}
