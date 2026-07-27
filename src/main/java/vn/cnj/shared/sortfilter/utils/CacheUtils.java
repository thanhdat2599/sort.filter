package vn.cnj.shared.sortfilter.utils;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

/**
 * Utility class for caching operations
 */
@Configuration
public class CacheUtils {
    
    private static final String SORT_FILTER_CACHE = "sortFilterCache";
    
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(SORT_FILTER_CACHE);
    }
    
    /**
     * Gets a value from the cache
     *
     * @param cacheManager the cache manager
     * @param key the cache key
     * @return the cached value, or empty if not found
     */
    public static <T> Optional<T> getFromCache(CacheManager cacheManager, String key) {
        Cache cache = cacheManager.getCache(SORT_FILTER_CACHE);
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get(key);
            if (wrapper != null) {
                @SuppressWarnings("unchecked")
                T value = (T) wrapper.get();
                return Optional.ofNullable(value);
            }
        }
        return Optional.empty();
    }
    
    /**
     * Puts a value in the cache
     *
     * @param cacheManager the cache manager
     * @param key the cache key
     * @param value the value to cache
     */
    public static void putInCache(CacheManager cacheManager, String key, Object value) {
        Cache cache = cacheManager.getCache(SORT_FILTER_CACHE);
        if (cache != null) {
            cache.put(key, value);
        }
    }
    
    /**
     * Clears the cache
     *
     * @param cacheManager the cache manager
     */
    public static void clearCache(CacheManager cacheManager) {
        Cache cache = cacheManager.getCache(SORT_FILTER_CACHE);
        if (cache != null) {
            cache.clear();
        }
    }
} 