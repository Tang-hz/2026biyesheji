package com.gk.study.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Cache 缓存配置（内存版，不依赖 Redis）。
 * <p>
 * 配置商品列表和详情的缓存 TTL：
 * - thing:list  5分钟
 * - thing:detail 3分钟
 */
@Configuration
@EnableCaching
public class CacheRedisConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("thing:list", "thing:detail");
    }
}