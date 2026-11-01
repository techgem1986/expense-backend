package com.expenseapp.shared.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for caching in the application.
 * Uses an in-memory ConcurrentMapCacheManager as the cache store.
 * No external cache service (e.g. Redis) is required; the application runs
 * self-contained. For production multi-instance deployments, replace this bean
 * with a distributed CacheManager (e.g. Redis) and add the corresponding
 * dependency plus spring.cache.* / connection properties.
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