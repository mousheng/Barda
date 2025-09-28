package com.barda.domain.datasource.service.impl;

import org.springframework.stereotype.Service;

import com.barda.domain.datasource.model.Datasource;
import com.barda.domain.datasource.model.DatasourceConnectionHolder;
import com.barda.domain.datasource.model.StatelessDatasourceConnectionHolder;
import com.barda.domain.datasource.service.DatasourceConnectionPool;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 实现无状态数据源连接池的服务类。
 * 该类提供创建和获取无状态数据源连接的功能。
 */
@Service
@Slf4j
public class StatelessConnectionPool implements DatasourceConnectionPool {

    /**
     * 获取或创建数据源的无状态连接。
     *
     * @param datasource 数据源
     * @return 包含无状态数据源连接持有者的 Mono
     */
    @Override
    public Mono<? extends DatasourceConnectionHolder> getOrCreateConnection(Datasource datasource) {
        return Mono.just(new StatelessDatasourceConnectionHolder());
    }

    /**
     * 获取数据源的相关信息。
     *
     * 请注意，此方法在无状态数据源连接池中未实现。
     *
     * @param datasourceId 数据源 ID
     * @return 未实现的 UnsupportedOperationException
     */
    @Override
    public Object info(String datasourceId) {
        throw new UnsupportedOperationException();
    }
}

