package com.shelf.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;

/**
 * 缓存配置类 - 优化版本
 */
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("stats", "products", "stockData");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000) // 最大缓存条目数
                .expireAfterWrite(5, TimeUnit.MINUTES) // 写入后5分钟过期
                .expireAfterAccess(2, TimeUnit.MINUTES) // 访问后2分钟过期
                .recordStats()); // 启用统计信息
        return cacheManager;
    }
    
    @Bean("productCacheManager")
    public CacheManager productCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("productOptions");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(500) // 商品选项缓存
                .expireAfterWrite(10, TimeUnit.MINUTES) // 商品选项缓存10分钟
                .recordStats());
        return cacheManager;
    }
}