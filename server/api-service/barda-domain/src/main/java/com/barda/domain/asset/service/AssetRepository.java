package com.barda.domain.asset.service;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.barda.domain.asset.model.Asset;

/**
 * 资产仓库接口，继承了 ReactiveMongoRepository 用于操作 MongoDB 数据库中的 Asset 集合。
 *
 * 该接口定义了对 Asset 对象的 CRUD (Create, Read, Update, Delete) 操作，
 * 并提供了基于 Reactive Streams 的非阻塞式 API。
 *
 * 注意：在使用该接口时，需要在 Spring Boot 应用中配置 ReactiveMongoTemplate 或者 ReactiveMongoDatabaseFactory。
 */
public interface AssetRepository extends ReactiveMongoRepository<Asset, String> {
}
