package com.barda.domain.application.repository;

import com.barda.domain.application.model.Application;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 自定义应用数据访问层接口。
 * 该接口定义了一些不在 ReactiveMongoRepository 接口中定义的特定于应用的查询方法。
 */
public interface CustomApplicationRepository {

    /**
     * 根据组织 ID 查询应用列表，使用 Domain-Specific Language (DSL) 查询。
     *
     * @param organizationId 组织 ID
     * @return 应用列表的 Flux
     */
    Flux<Application> findByOrganizationIdWithDsl(String organizationId);

    /**
     * 根据 ID 查询应用，使用 Domain-Specific Language (DSL) 查询。
     *
     * @param applicationId 应用 ID
     * @return 应用的 Mono
     */
    Mono<Application> findByIdWithDsl(String applicationId);
}
