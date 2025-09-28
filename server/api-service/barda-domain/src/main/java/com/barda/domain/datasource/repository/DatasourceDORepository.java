package com.barda.domain.datasource.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.barda.domain.datasource.model.DatasourceDO;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 该接口定义了对 DatasourceDO 对象的 Reactive MongoDB 存储库的操作。
 * 它继承了 ReactiveMongoRepository 接口，并指定了 DatasourceDO 作为操作的实体类，String 作为 ID 的类型。
 */
public interface DatasourceDORepository extends ReactiveMongoRepository<DatasourceDO, String> {

    /**
     * 根据组织 ID 查询所有数据源。
     *
     * @param organizationId 组织 ID
     * @return 包含所有数据源的 Flux 对象
     */
    Flux<DatasourceDO> findAllByOrganizationId(String organizationId);

    /**
     * 根据组织 ID、数据源类型和创建来源查询单个数据源。
     *
     * @param organizationId 组织 ID
     * @param type 数据源类型
     * @param creationSource 创建来源
     * @return 包含查询结果的 Mono 对象
     */
    Mono<DatasourceDO> findByOrganizationIdAndTypeAndCreationSource(String organizationId, String type, int creationSource);

    /**
     * 根据组织 ID 查询数据源的数量。
     *
     * @param organizationId 组织 ID
     * @return 包含查询结果的 Mono 对象，其中包含 Long 类型的值
     */
    Mono<Long> countByOrganizationId(String organizationId);
}
