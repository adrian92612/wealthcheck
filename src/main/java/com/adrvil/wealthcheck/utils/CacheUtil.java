package com.adrvil.wealthcheck.utils;

import com.adrvil.wealthcheck.enums.CacheName;
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

//    public void evictAll(String cacheName) {
//        Cache cache = cacheManager.getCache(cacheName);
//        if (cache != null) {
//            cache.clear();
//        }
//    }

    public void evictOverviewCaches(Long userId) {
        String key = String.valueOf(userId);
        evict(CacheName.USER_TRANSACTIONS.getValue(), key);
        evict(CacheName.OVERVIEW.getValue(), key);
        evict(CacheName.TOP_TRANSACTIONS.getValue(), key);
        evict(CacheName.RECENT_TRANSACTIONS.getValue(), key);
        evict(CacheName.DAILY_NET.getValue(), key);
        evict(CacheName.TOP_CATEGORIES.getValue(), key);
    }

    public void evictWalletCaches(Long userId, Long walletId) {
        if (walletId != null) {
            evict(CacheName.WALLET.getValue(), userId + ":" + walletId);
        }
        evict(CacheName.USER_WALLETS.getValue(), String.valueOf(userId));
    }


}
