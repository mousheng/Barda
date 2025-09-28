package com.barda.plugin.redis;

import com.barda.sdk.exception.PluginError;

/**
 * 定义了与 Redis 相关的错误类型。
 */
public enum RedisError implements PluginError {

    /**
     * 指示在执行 Redis 命令时发生的错误。
     */
    REDIS_EXECUTION_ERROR,

    /**
     * 指示 Redis URI 格式不正确。
     */
    REDIS_URI_ERROR,

}
