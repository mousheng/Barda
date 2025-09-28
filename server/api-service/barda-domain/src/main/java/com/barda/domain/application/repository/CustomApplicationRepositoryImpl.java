package com.barda.domain.application.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.barda.domain.application.model.Application;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 自定义应用数据访问层实现类。
 * 该类实现了 CustomApplicationRepository 接口，并提供基于 Domain-Specific Language (DSL) 的查询方法。
 */
@Repository
public class CustomApplicationRepositoryImpl implements CustomApplicationRepository {

    /**
     * 反应式 MongoDB 模板，用于执行 MongoDB 查询。
     */
    @Autowired
    private ReactiveMongoTemplate reactiveMongoTemplate;

    /**
     * {@inheritDoc}
     *
     * 根据组织 ID 查询应用列表，使用 Domain-Specific Language (DSL) 查询。
     *
     * @param organizationId 组织 ID
     * @return 应用列表的 Flux
     */
    @Override
    public Flux<Application> findByOrganizationIdWithDsl(String organizationId) {
        Criteria criteria = Criteria.where("organizationId").is(organizationId);
        Query query = new Query(criteria);
        return reactiveMongoTemplate.find(query, Application.class);
    }

    /**
     * {@inheritDoc}
     *
     * 根据 ID 查询应用，使用 Domain-Specific Language (DSL) 查询。
     *
     * @param applicationId 应用 ID
     * @return 应用的 Mono
     */
    @Override
    public Mono<Application> findByIdWithDsl(String applicationId) {
        return reactiveMongoTemplate.findById(applicationId, Application.class);
    }
}
