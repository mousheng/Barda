package com.barda.domain.template.repository;

import java.util.Collection;

import javax.annotation.Nonnull;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.barda.domain.template.model.Template;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 模板仓库接口，用于操作 MongoDB 中的模板集合。
 * 该接口继承了 ReactiveMongoRepository，提供了一组基础的 CRUD 操作。
 * 该接口使用了 Spring Data MongoDB 库来实现对 MongoDB 集合的操作。
 */
@Repository
public interface TemplateRepository extends ReactiveMongoRepository<Template, String> {

    /**
     * 根据 ID 查询模板。
     *
     * @param id 模板 ID
     * @return 模板 Mono 对象
     */
    @Nonnull
    Mono<Template> findById(@Nonnull String id);

    /**
     * 根据应用 ID 集合查询模板。
     *
     * @param applicationId 应用 ID 集合
     * @return 模板 Flux 对象
     */
    Flux<Template> findByApplicationIdIn(Collection<String> applicationId);

    /**
     * 根据应用 ID 查询模板。
     *
     * @param applicationId 应用 ID
     * @return 模板 Mono 对象
     */
    Mono<Template> findByApplicationId(String applicationId);
}
