package com.barda.domain.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import es.moki.ratelimitj.redis.request.RedisRateLimiterFactory;
import io.lettuce.core.RedisClient;

/**
 * Redis 限流器配置类。
 *
 * 该类提供 Redis 限流器相关的配置和 bean 定义。
 */
@Configuration
public class RedisRateLimiterConfig {

    /**
     * Redis 连接 URL。
     *
     * 该值可以通过 application.properties 文件进行配置。
     * 默认值为 "redis"。
     */
    @Value("${spring.data.redis.url:redis}")
    private String redis;

    /**
     * 创建并返回 RedisRateLimiterFactory bean。
     *
     * RedisRateLimiterFactory 用于创建 Redis 限流器。
     *
     * @return 已配置的 RedisRateLimiterFactory 实例
     */
    @Bean
    RedisRateLimiterFactory build() {
        return new RedisRateLimiterFactory(RedisClient.create(redis));
    }
}