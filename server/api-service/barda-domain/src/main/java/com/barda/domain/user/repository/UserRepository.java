package com.barda.domain.user.repository;

import java.util.Collection;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.barda.domain.user.model.User;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 用户仓库接口，继承自ReactiveMongoRepository，用于操作User集合。
 */
@Repository
public interface UserRepository extends ReactiveMongoRepository<User, String> {

    /**
     * 根据用户ID集合查询用户
     * @param ids 用户ID集合
     * @return Flux<User>
     */
    Flux<User> findByIdIn(Collection<String> ids);

    /**
     * 根据连接的来源和原始ID查询用户
     * @param source 连接的来源
     * @param rawId 原始ID
     * @return Mono<User>
     */
    Mono<User> findByConnections_SourceAndConnections_RawId(String source, String rawId);

    /**
     * 根据连接的来源和原始ID集合查询用户
     * @param source 连接的来源
     * @param rawIds 原始ID集合
     * @return Flux<User>
     */
    Flux<User> findByConnections_SourceAndConnections_RawIdIn(String source, Collection<String> rawIds);
}