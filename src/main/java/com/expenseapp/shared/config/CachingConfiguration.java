package com.expenseapp.shared.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for caching in the application.
 * Uses an in-memory ConcurrentMapCacheManager as the cache store.
 * Redis integration is available when Redis dependencies and connection factory
 * are present in the classpath. To enable Redis caching, set the appropriate
 * spring.cache.type and spring.redis.* properties in the application config.
 */
@Configuration
@EnableCaching
public class CachingConfiguration {

    /**
     * In-memory cache manager that works without any external cache service.
     * Suitable for development and single-instance deployments.
     * For production multi-instance deployments, consider switching to Redis.
     */
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager();
    }
}