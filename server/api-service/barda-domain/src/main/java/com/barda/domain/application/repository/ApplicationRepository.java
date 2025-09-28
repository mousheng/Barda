package com.barda.domain.application.repository;


import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.barda.domain.application.model.Application;
import com.barda.domain.application.model.ApplicationStatus;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 应用数据访问层接口，继承了 ReactiveMongoRepository 和 CustomApplicationRepository。
 * 该接口定义了对 Application 实体的常用数据操作。
 */
@Repository
public interface ApplicationRepository extends ReactiveMongoRepository<Application, String>, CustomApplicationRepository {

    /**
     * 根据组织 ID 查询应用列表，并排除 publishedApplicationDSL 和 editingApplicationDSL 字段。
     *
     * @param organizationId 组织 ID
     * @return 应用列表的 Flux
     */
    @Query(fields = "{ publishedApplicationDSL : 0 , editingApplicationDSL : 0 }")
    Flux<Application> findByOrganizationId(String organizationId);

    /**
     * 根据 ID 查询应用，并排除 publishedApplicationDSL 和 editingApplicationDSL 字段。
     *
     * @param id 应用 ID
     * @return 应用的 Mono
     */
    @Override
    @Nonnull
    @Query(fields = "{ publishedApplicationDSL : 0, editingApplicationDSL : 0 }")
    Mono<Application> findById(@Nonnull String id);

    /**
     * 统计指定组织 ID 和应用状态的应用数量。
     *
     * @param organizationId 组织 ID
     * @param applicationStatus 应用状态
     * @return 应用数量的 Mono
     */
    Mono<Long> countByOrganizationIdAndApplicationStatus(String organizationId, ApplicationStatus applicationStatus);

    /**
     * 查询包含指定数据源 ID 的应用列表，并排除 publishedApplicationDSL 和 editingApplicationDSL 字段。
     *
     * @param datasourceId 数据源 ID
     * @return 应用列表的 Flux
     */
    @Query("{$or : [{'publishedApplicationDSL.queries.datasourceId':?0},{'editingApplicationDSL.queries.datasourceId':?0}]}")
    Flux<Application> findByDatasourceId(String datasourceId);

    /**
     * 根据 ID 列表查询应用列表。
     *
     * @param ids ID 列表
     * @return 应用列表的 Flux
     */
    Flux<Application> findByIdIn(List<String> ids);

    /**
     * 查询在公共中可见的应用列表，并只返回 ID 字段。
     *
     * @param ids ID 集合
     * @return 应用列表的 Flux
     */
    @Query(fields = "{_id : 1}")
    Flux<Application> findByPublicToAllIsTrueAndIdIn(Collection<String> ids);

}
