package com.barda.infra.birelation;

import java.util.Collection;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 双向关系的 MongoDB 存储库接口。
 *
 * 该接口继承了 ReactiveMongoRepository，并指定了 BiRelation 作为实体类型和 String 作为 ID 类型。
 * 它提供了丰富的查询方法来操作 BiRelation 集合。
 *
 */
public interface BiRelationRepository extends ReactiveMongoRepository<BiRelation, String> {

    /**
     * 根据业务类型和源 ID 查询双向关系。
     *
     * @param bizType 业务类型
     * @param sourceId 源 ID
     * @return 双向关系的 Flux 对象
     */
    Flux<BiRelation> findByBizTypeAndSourceId(BiRelationBizType bizType, String sourceId);

    /**
     * 根据业务类型、源 ID 和分页信息查询双向关系。
     *
     * @param bizType 业务类型
     * @param sourceId 源 ID
     * @param pageable 分页信息
     * @return 双向关系的 Flux 对象
     */
    Flux<BiRelation> findByBizTypeAndSourceId(BiRelationBizType bizType, String sourceId, Pageable pageable);

    /**
     * 根据业务类型和多个源 ID 查询双向关系。
     *
     * @param bizType 业务类型
     * @param sourceId 源 ID 集合
     * @return 双向关系的 Flux 对象
     */
    Flux<BiRelation> findByBizTypeAndSourceIdIn(BiRelationBizType bizType, Collection<String> sourceId);

    /**
     * 根据业务类型和目标 ID 查询双向关系。
     *
     * @param bizType 业务类型
     * @param targetId 目标 ID
     * @return 双向关系的 Flux 对象
     */
    Flux<BiRelation> findByBizTypeAndTargetId(BiRelationBizType bizType, String targetId);

    /**
     * 根据业务类型和多个目标 ID 查询双向关系。
     *
     * @param bizType 业务类型
     * @param targetId 目标 ID 集合
     * @return 双向关系的 Flux 对象
     */
    Flux<BiRelation> findByBizTypeAndTargetIdIn(BiRelationBizType bizType, Collection<String> targetId);

    /**
     * 根据业务类型、源 ID 和目标 ID 查询双向关系。
     *
     * @param bizType 业务类型
     * @param sourceId 源 ID
     * @param targetId 目标 ID
     * @return 双向关系的 Mono 对象
     */
    Mono<BiRelation> findByBizTypeAndSourceIdAndTargetId(BiRelationBizType bizType, String sourceId, String targetId);

    /**
     * 根据业务类型、目标 ID 和多个源 ID 查询双向关系。
     *
     * @param bizType 业务类型
     * @param targetId 目标 ID
     * @param sourceId 源 ID 集合
     * @return 双向关系的 Flux 对象
     */
    Flux<BiRelation> findByBizTypeAndTargetIdAndSourceIdIn(BiRelationBizType bizType, String targetId, Collection<String> sourceId);

    /**
     * 根据业务类型、源 ID 和关系查询双向关系。
     *
     * @param bizType 业务类型
     * @param sourceId 源 ID
     * @param relation 关系
     * @return 双向关系的 Flux 对象
     */
    Flux<BiRelation> findByBizTypeAndSourceIdAndRelation(BiRelationBizType bizType, String sourceId, String relation);

    /**
     * 根据业务类型和源 ID 统计双向关系的数量。
     *
     * @param bizType 业务类型
     * @param sourceId 源 ID
     * @return 双向关系的数量的 Mono 对象
     */
    Mono<Long> countByBizTypeAndSourceId(BiRelationBizType bizType, String sourceId);

    /**
     * 根据业务类型和目标 ID 统计双向关系的数量。
     *
     * @param bizType 业务类型
     * @param targetId 目标 ID
     * @return 双向关系的数量的 Mono 对象
     */
    Mono<Long> countByBizTypeAndTargetId(BiRelationBizType bizType, String targetId);
}
