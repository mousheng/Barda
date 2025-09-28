package com.barda.plugin.redis.constants;

/**
 * 定义了 Redis 相关的常量。
 */
public final class RedisConstants {

    /**
     * 用于测试的超时时间，单位为毫秒。
     */
    public static final long TEST_TIMEOUT_MILLIS = 2000;

    /**
     * Jedis 连接池的最大连接数。
     */
    public static final int JEDIS_POOL_MAX_TOTAL = 10;

    /**
     * Jedis 连接池的最大空闲连接数。
     */
    public static final int JEDIS_POOL_MAX_IDLE = 10;

    /**
     * Jedis 连接池的最小空闲连接数。
     */
    public static final int JEDIS_POOL_MIN_IDLE = 0;

    /**
     * Jedis 连接池中可被逐出的最小空闲连接的空闲时间，单位为毫秒。
     */
    public static final long JEDIS_POOL_MIN_EVICTABLE_IDLE_MILLIS = 60000;

    /**
     * Jedis 连接池执行空闲连接回收器的周期，单位为毫秒。
     */
    public static final long JEDIS_POOL_TIME_BETWEEN_EVICTION_RUNS_MILLIS = 30000;
}
