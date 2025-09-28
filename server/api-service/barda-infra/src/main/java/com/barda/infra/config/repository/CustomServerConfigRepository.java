package com.barda.infra.config.repository;

import com.barda.infra.config.model.ServerConfig;

import reactor.core.publisher.Mono;

/**
 * 自定义的服务器配置仓库接口。
 *
 * 该接口定义了与服务器配置相关的操作，并使用 Spring Data MongoDB 来实现数据访问。
 *
 */
interface CustomServerConfigRepository {

    /**
     * 基于 upsert 机制更新或插入单个服务器配置。
     *
     * 该方法使用键来标识服务器配置，并使用值来更新或插入配置。
     * 如果键在数据库中存在，则更新该键的值；如果键在数据库中不存在，则插入该键的值。
     *
     * @param key 键
     * @param value 值
     * @return 包含更新或插入的服务器配置的 Mono 对象
     */
    Mono<ServerConfig> upsert(String key, Object value);
}
