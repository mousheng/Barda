package com.barda.infra.perf;

/**
 * 性能事件枚举，定义了各种性能事件类型。
 */
public enum PerfEvent {
    /**
     * API错误代码事件。
     */
    API_ERROR_CODE,

    /**
     * 插件错误代码事件。
     */
    PLUGIN_ERROR_CODE,

    /**
     * IO心跳事件。
     */
    IO_HEART_BEAT,

    /**
     * 审计日志批量插入事件。
     */
    AUDIT_LOG_BATCH_INSERT,

    /**
     * 服务器日志批量插入事件。
     */
    SERVER_LOG_BATCH_INSERT,

    /**
     * 发送短信事件。
     */
    SEND_SMS,

    /**
     * 基于客户端的连接创建事件。
     */
    CLIENT_BASED_CONNECTION_CREATE,

    /**
     * 基于客户端的连接移除事件。
     */
    CLIENT_BASED_CONNECTION_REMOVE,

    /**
     * 基于客户端的连接数量事件。
     */
    CLIENT_BASED_CONNECTION_SIZE,

    /**
     * Hikari连接池中的总连接数事件。
     */
    HIKARI_POOL_TOTAL_CONNECTIONS,

    /**
     * Hikari连接池中的活动连接数事件。
     */
    HIKARI_POOL_ACTIVE_CONNECTIONS,

    /**
     * Hikari连接池中的等待连接数事件。
     */
    HIKARI_POOL_WAITING_CONNECTIONS,

    /**
     * Hikari连接池中的空闲连接数事件。
     */
    HIKARI_POOL_IDLE_CONNECTIONS;

    /**
     * 获取性能事件的性能键。
     *
     * @return 性能键。
     */
    public String perfKey() {
        return name().toLowerCase();
    }
}

