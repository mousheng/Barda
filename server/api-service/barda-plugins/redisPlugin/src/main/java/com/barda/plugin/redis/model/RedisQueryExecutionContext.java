package com.barda.plugin.redis.model;

import com.barda.sdk.query.QueryExecutionContext;

import lombok.Builder;
import lombok.Getter;
import redis.clients.jedis.Protocol;

/**
 * 定义了 Redis 查询执行上下文。
 * 继承自 QueryExecutionContext，提供 Redis 查询执行所需的上下文信息。
 */
@Getter
@Builder
public class RedisQueryExecutionContext extends QueryExecutionContext {

    /**
     * Redis 协议命令。
     */
    private Protocol.Command protocolCommand;

    /**
     * Redis 命令参数。
     */
    private String[] args;
}
