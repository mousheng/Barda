package com.barda.domain.datasource.repository;


import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.barda.domain.datasource.model.DatasourceStructureDO;

import reactor.core.publisher.Mono;

/**
 * 该接口定义了对数据源结构的CRUD操作，基于Reactive MongoDB。
 * 它继承了ReactiveMongoRepository，提供基础的CRUD方法，并添加了自定义的findByDatasourceId方法。
 */
public interface DatasourceStructureRepository extends ReactiveMongoRepository<DatasourceStructureDO, String> {

    /**
     * 根据数据源ID查询数据源结构。
     *
     * @param datasourceId 要查询的数据源ID
     * @return 包含数据源结构对象的Mono
     */
    Mono<DatasourceStructureDO> findByDatasourceId(String datasourceId);
}
