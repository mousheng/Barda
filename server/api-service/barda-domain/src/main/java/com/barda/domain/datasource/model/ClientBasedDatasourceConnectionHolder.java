package com.barda.domain.datasource.model;

import com.barda.sdk.exception.InvalidHikariDatasourceException;

import lombok.extern.slf4j.Slf4j;

/**
 * 基于客户端的数据库连接持有者类。
 * 该类实现了 {@link DatasourceConnectionHolder} 接口，
 * 并提供对客户端错误的检测和处理。
 */
@Slf4j
public class ClientBasedDatasourceConnectionHolder implements DatasourceConnectionHolder {

    /**
     * 指示是否发生了客户端错误。
     * 该字段使用 {@code volatile} 关键字保证线程安全。
     */
    private volatile boolean clientErrorOccurred;

    /**
     * 数据库连接对象。
     */
    private final Object connection;

    /**
     * 构造函数。
     *
     * @param connection 数据库连接对象
     */
    public ClientBasedDatasourceConnectionHolder(Object connection) {
        this.connection = connection;
    }

    /**
     * 检查数据库连接是否过时。
     *
     * @return 如果发生了客户端错误，返回 {@code true}，否则返回 {@code false}
     */
    public boolean isStale() {
        return clientErrorOccurred;
    }

    /**
     * 获取数据库连接对象。
     *
     * @return 数据库连接对象
     */
    @Override
    public Object connection() {
        return connection;
    }

    /**
     * 处理查询时发生的错误。
     * 如果发生了 {@link InvalidHikariDatasourceException}，
     * 记录错误日志并将 {@link #clientErrorOccurred} 设置为 {@code true}。
     *
     * @param throwable 发生的错误
     */
    @Override
    public void onQueryError(Throwable throwable) {
        if (throwable instanceof InvalidHikariDatasourceException) {
            log.error("client based connection error.", throwable);
            clientErrorOccurred = true;
        }
    }
}
