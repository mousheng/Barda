package com.barda.domain.datasource.model;

import java.time.Instant;

/**
 * 该类实现了 DatasourceConnectionHolder 接口，用于提供基于令牌的连接。
 * 它包含一个 EMPTY_CONNECTION 常量，表示空的连接。
 */
public class TokenBasedConnectionHolder implements DatasourceConnectionHolder {

    /**
     * 一个 EMPTY_CONNECTION 常量，表示空的连接。
     */
    public static final TokenBasedConnectionHolder EMPTY_CONNECTION = new TokenBasedConnectionHolder(null);

    /**
     * 基于令牌的连接。
     */
    private final TokenBasedConnection connection;

    /**
     * 构造函数，根据传入的 TokenBasedConnection 创建 TokenBasedConnectionHolder。
     *
     * @param tokenBasedConnection 基于令牌的连接
     */
    public TokenBasedConnectionHolder(TokenBasedConnection tokenBasedConnection) {
        this.connection = tokenBasedConnection;
    }

    /**
     * 检查该连接是否已过期。
     *
     * @param datasourceUpdateTime 数据源的更新时间
     * @return true 如果该连接已过期，false 否则
     */
    public boolean isStale(Instant datasourceUpdateTime) {

        if (connection == null) {
            return true;
        }

        Instant connectionUpdateTime = connection.getUpdatedAt();
        if (datasourceUpdateTime != null && datasourceUpdateTime.isAfter(connectionUpdateTime)) {
            return true;
        }

        return connection.isStale();

    }

    /**
     * 该方法在执行查询时发生错误时被调用。
     * 目前，该方法什么都不做。
     *
     * @param throwable 发生的错误
     */
    @Override
    public void onQueryError(Throwable throwable) {
    }

    /**
     * 获取数据源的连接。
     *
     * @return 基于令牌的连接的详细信息
     */
    @Override
    public Object connection() {
        return connection.getTokenDetail();
    }

}
