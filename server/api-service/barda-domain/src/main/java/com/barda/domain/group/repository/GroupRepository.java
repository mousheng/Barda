package com.barda.domain.group.repository;

import java.util.Collection;

import javax.validation.constraints.NotNull;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.barda.domain.group.model.Group;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 该接口表示用于管理 {@link Group} 实体的 MongoDB 数据库存储库。
 * 它扩展了 Spring Data MongoDB 提供的 {@link ReactiveMongoRepository} 接口，
 * 允许进行反应式 CRUD 操作和自定义查询。
 */
@Repository
public interface GroupRepository extends ReactiveMongoRepository<Group, String> {

    /**
     * 通过 ID 集合查找组。
     *
     * @param id 组 ID 集合
     * @return 与给定 ID 集合匹配的 {@link Group} 实体的 {@link Flux}
     */
    Flux<Group> findByIdIn(Collection<String> id);

    /**
     * 通过组织 ID 查找组。
     *
     * @param organizationId 组织的 ID
     * @return 与给定组织 ID 匹配的 {@link Group} 实体的 {@link Flux}
     */
    Flux<Group> findByOrganizationId(String organizationId);

    /**
     * 按组织 ID 计数组。
     *
     * @param organizationId 组织的 ID
     * @return 与给定组织 ID 匹配的组的数量的 {@link Mono}
     */
    Mono<Long> countByOrganizationId(@NotNull String organizationId);

    /**
     * 通过组织 ID 和 allUsersGroup 字段查找组。
     *
     * @param organizationId 组织的 ID
     * @param allUsersGroup  allUsersGroup 字段的值
     * @return 与给定组织 ID 和 allUsersGroup 字段匹配的 {@link Group} 实体的 {@link Mono}
     */
    Mono<Group> findByOrganizationIdAndAllUsersGroup(String organizationId, boolean allUsersGroup);

    /**
     * 通过组织 ID 和类型查找组。
     *
     * @param organizationId 组织的 ID
     * @param type           组的类型
     * @return 与给定组织 ID 和类型匹配的 {@link Group} 实体的 {@link Mono}
     */
    Mono<Group> findByOrganizationIdAndType(String organizationId, String type);

    /**
     * 通过来源和组织 ID 查找组。
     *
     * @param source         组的来源
     * @param organizationId 组织的 ID
     * @return 与给定来源和组织 ID 匹配的 {@link Group} 实体的 {@link Flux}
     */
    Flux<Group> findBySourceAndOrganizationId(String source, String organizationId);
}

