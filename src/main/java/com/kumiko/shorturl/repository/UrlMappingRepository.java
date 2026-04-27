package com.kumiko.shorturl.repository;

import com.kumiko.shorturl.model.UrlMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {
    Optional<UrlMapping> findByShortCode(String shortCode);

    @Query("SELECT u.shortCode FROM UrlMapping u")
    List<String> findAllShortCodes();
}
