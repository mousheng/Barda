package com.barda.domain.query.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.barda.domain.query.model.LibraryQuery;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 库查询的MongoDB Repository接口。
 * 它扩展了ReactiveMongoRepository<LibraryQuery, String>，提供对库查询的CRUD操作。
 */
@Repository
public interface LibraryQueryRepository extends ReactiveMongoRepository<LibraryQuery, String> {

    /**
     * 根据组织ID查询库查询。
     *
     * @param organizationId 组织ID
     * @return 符合组织ID的库查询的Flux流
     */
    Flux<LibraryQuery> findByOrganizationId(String organizationId);

    /**
     * 查询指定名称的库查询。
     *
     * @param name 库查询名称
     * @return 符合库查询名称的库查询
     */
    Mono<LibraryQuery> findByName(String name);
}
