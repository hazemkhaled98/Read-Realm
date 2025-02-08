package com.readrealm.catalog.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/refresh")
@RequiredArgsConstructor
@Profile("!test")
public class CacheController {

    private final CacheManager cacheManager;


    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void evictAllCaches() {
        cacheManager.getCacheNames().forEach(cacheName ->
                cacheManager.getCache(cacheName).invalidate()
        );
    }

}
