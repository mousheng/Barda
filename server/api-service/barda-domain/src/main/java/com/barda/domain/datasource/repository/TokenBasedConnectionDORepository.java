package com.barda.domain.datasource.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.barda.domain.datasource.model.TokenBasedConnectionDO;

import reactor.core.publisher.Mono;


/**
 * 该接口定义了对基于令牌的连接的CRUD操作，基于Reactive MongoDB。
 * 它继承了ReactiveMongoRepository，提供基础的CRUD方法，并添加了自定义的findByDatasourceId方法。
 */
public interface TokenBasedConnectionDORepository extends ReactiveMongoRepository<TokenBasedConnectionDO, String> {

    /**
     * 根据数据源ID查询基于令牌的连接。
     *
     * @param datasourceId 要查询的数据源ID
     * @return 包含基于令牌的连接对象的Mono
     */
    Mono<TokenBasedConnectionDO> findByDatasourceId(String datasourceId);
}
