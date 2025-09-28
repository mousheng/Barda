package com.barda.infra.config.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.google.common.annotations.VisibleForTesting;
import com.barda.infra.config.model.ServerConfig;

import reactor.core.publisher.Mono;

/**
 * 服务器配置仓库接口，继承自 ReactiveMongoRepository 并实现了自定义的 CustomServerConfigRepository 接口。
 *
 * 该接口用于定义与服务器配置相关的操作，并使用 Spring Data MongoDB 来实现数据访问。
 *
 * 注意：该接口使用 {@link org.springframework.data.annotation.VisibleForTesting} 注解来标记为仅在测试中可见。
 *
 */
@VisibleForTesting
public interface ServerConfigRepository extends ReactiveMongoRepository<ServerConfig, String>, CustomServerConfigRepository {

    /**
     * 根据键查询单个服务器配置。
     *
     * @param key 键
     * @return 包含查询到的服务器配置的 Mono 对象
     */
    Mono<ServerConfig> findByKey(String key);
}
