package com.barda.domain.datasource.service;

import com.barda.sdk.models.DatasourceStructure;

import reactor.core.publisher.Mono;

/**
 * 数据源结构服务接口。
 *
 * 该接口定义了与数据源结构相关的操作，包括获取数据源结构等功能。
 */
public interface DatasourceStructureService {

    /**
     * 获取数据源的结构。
     *
     * @param datasourceId 要获取结构的数据源的 ID
     * @param ignoreCache 是否忽略缓存
     * @return 获取的数据源结构的 Mono
     */
    Mono<DatasourceStructure> getStructure(String datasourceId, boolean ignoreCache);
}
