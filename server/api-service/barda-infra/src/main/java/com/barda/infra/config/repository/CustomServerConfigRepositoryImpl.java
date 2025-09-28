package com.barda.infra.config.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.barda.infra.config.model.ServerConfig;
import com.barda.infra.mongo.MongoUpsertHelper;

import reactor.core.publisher.Mono;

/**
 * 自定义的服务器配置仓库实现类，实现了 CustomServerConfigRepository 接口。
 *
 * 该类使用 Spring Data MongoDB 的 @Repository 注解来标记为数据访问层的类。
 *
 */
@Repository
class CustomServerConfigRepositoryImpl implements CustomServerConfigRepository {

    /**
     * 用于 MongoDB upsert 操作的辅助类。
     *
     * 该类使用 Spring 的 @Autowired 注解来注入 MongoUpsertHelper 实例。
     */
    @Autowired
    private MongoUpsertHelper mongoUpsertHelper;

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
    @Override
    public Mono<ServerConfig> upsert(String key, Object value) {
        ServerConfig newConfig = ServerConfig.builder()
                .key(key)
                .value(value)
                .build();
        return mongoUpsertHelper.upsertWithAuditingParams(newConfig, "key", key);
    }
}
