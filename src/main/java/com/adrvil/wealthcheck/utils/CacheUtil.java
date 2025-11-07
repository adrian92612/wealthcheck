package com.adrvil.wealthcheck.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CacheUtil {
    private final CacheManager cacheManager;

    @SuppressWarnings("unchecked")
    public <T> T get(String cacheName, String key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get(key);
            return wrapper != null ? (T) wrapper.get() : null;
        }
        return null;
    }

    public void put(String cacheName, String key, Object value) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.put(key, value);
        }
    }

    public void evict(String cacheName, String key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
        }
    }

    public void evictAll(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }
}
