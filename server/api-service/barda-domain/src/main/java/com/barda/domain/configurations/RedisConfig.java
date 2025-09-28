package com.barda.domain.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import lombok.extern.slf4j.Slf4j;

/**
 * Redis 配置类。
 *
 * 该类提供 Redis 相关的配置和 bean 定义。
 */
@Configuration
@Slf4j
public class RedisConfig {

    /**
     * 创建并返回 ReactiveRedisOperations<String, String> bean。
     *
     * 该 bean 用于在 Reactive 编程模型中与 Redis 进行交互。
     *
     * @param factory 用于创建 ReactiveRedisConnectionFactory 的工厂
     * @return 已配置的 ReactiveRedisOperations<String, String> 实例
     */
    @Primary
    @Bean
    ReactiveRedisOperations<String, String> reactiveRedisOperations(ReactiveRedisConnectionFactory factory) {

        var context = RedisSerializationContext.<String, String> newSerializationContext(new StringRedisSerializer())
                .value(new Jackson2JsonRedisSerializer<>(String.class))
                .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }
}
