package com.demo.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class AppConfig {

//    @Bean
//    public CacheManager cacheManager() {
//
//        CaffeineCacheManager cacheManager = new CaffeineCacheManager("products");
//
//        cacheManager.setCaffeine(
//                Caffeine.newBuilder()
//                        .initialCapacity(10)
//                        .maximumSize(100)
//                        .expireAfterWrite(10, TimeUnit.MINUTES)
//        );
//
//        return cacheManager;
//    }
}
