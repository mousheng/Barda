package com.barda.domain.datasource.service;

import javax.annotation.Nullable;

import com.barda.domain.datasource.model.Datasource;
import com.barda.domain.datasource.model.DatasourceConnectionHolder;

import reactor.core.publisher.Mono;

/**
 * 数据源连接池接口。
 *
 * 该接口定义了与数据源连接相关的操作，包括获取或创建连接和获取信息等功能。
 */
public interface DatasourceConnectionPool {

    /**
     * 获取或创建数据源的连接。
     *
     * @param datasource 要获取或创建连接的数据源
     * @return 获取或创建的连接的 Mono
     */
    Mono<? extends DatasourceConnectionHolder> getOrCreateConnection(Datasource datasource);

    /**
     * 获取数据源的相关信息。
     *
     * @param datasourceId 要获取信息的数据源的 ID (可选)
     * @return 获取的信息
     */
    Object info(@Nullable String datasourceId);
}
