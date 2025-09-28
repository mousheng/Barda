package com.barda.infra.birelation;

import static com.google.common.base.Strings.nullToEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.data.mongodb.core.query.Criteria.where;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import com.barda.infra.mongo.MongoUpsertHelper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 双向关系的业务逻辑类。
 *
 * 该类使用 @Service 注解来标记为 Spring Bean，并使用 @Autowired 注解来注入所需的依赖项。
 *
 */
@Service
public class BiRelationService {

    private static final String BIZ_TYPE = "bizType";
    private static final String SOURCE_ID = "sourceId";
    private static final String TARGET_ID = "targetId";
    private static final String RELATION = "relation";

    @Autowired
    private BiRelationRepository biRelationRepository;

    @Autowired
    private MongoUpsertHelper mongoUpsertHelper;

    /**
     * 新增单个双向关系。
     *
     * @param biRelation 双向关系
     * @return 新增的双向关系的 Mono 对象
     */
    public Mono<BiRelation> addBiRelation(BiRelation biRelation) {
        return biRelationRepository.save(biRelation);
    }

    /**
     * 批量新增双向关系。
     *
     * @param biRelations 双向关系集合
     * @return 新增的双向关系的 Flux 对象
     */
    public Mono<List<BiRelation>> batchAddBiRelation(Collection<BiRelation> biRelations) {
        return biRelationRepository.saveAll(biRelations)
                .collectList();
    }

    // 以下是重载方法

    // 以下是其他方法

    /**
     * 新增单个双向关系。
     *
     * @param bizType 业务类型
     * @param sourceId 源 ID
     * @param targetId 目标 ID
     * @param relation 关系
     * @param state 状态
     * @return 新增的双向关系的 Mono 对象
     */
    public Mono<BiRelation> addBiRelation(BiRelationBizType bizType, String sourceId, String targetId,
            String relation, String state) {
        return addBiRelation(bizType, sourceId, targetId, relation, state, null, null, null);
    }

    /**
     * 添加双向关系。
     *
     * @param bizType 业务类型。
     * @param sourceId 源标识符。
     * @param targetId 目标标识符。
     * @param relation 关系类型。
     * @param state 状态。
     * @param extParam1 扩展参数1。
     * @return 表示添加操作结果的 {@code Mono<BiRelation>}，包含添加的双向关系对象。
     */
    public Mono<BiRelation> addBiRelation(BiRelationBizType bizType, String sourceId, String targetId,
            String relation, String state, String extParam1) {
        return addBiRelation(bizType, sourceId, targetId, relation, state, extParam1, null, null);
    }

    /**
     * 添加双向关系。
     *
     * @param bizType 业务类型。
     * @param sourceId 源标识符。
     * @param targetId 目标标识符。
     * @param relation 关系类型。
     * @param state 状态。
     * @param extParam1 扩展参数1。
     * @param extParam2 扩展参数2。
     * @return 表示添加操作结果的 {@code Mono<BiRelation>}，包含添加的双向关系对象。
     */
    public Mono<BiRelation> addBiRelation(BiRelationBizType bizType, String sourceId, String targetId,
            String relation, String state, String extParam1, String extParam2) {
        return addBiRelation(bizType, sourceId, targetId, relation, state, extParam1, extParam2, null);
    }

    /**
     * 添加双向关联关系。
     *
     * @param bizType    业务类型。
     * @param sourceId   源标识符。
     * @param targetId   目标标识符。
     * @param relation   关系类型。
     * @param state      状态。
     * @param extParam1  扩展参数1。
     * @param extParam2  扩展参数2。
     * @param extParam3  扩展参数3。
     * @return 表示添加的双向关联关系的 {@code Mono<BiRelation>} 对象。
     */
    public Mono<BiRelation> addBiRelation(BiRelationBizType bizType, String sourceId, String targetId,
            String relation, String state, String extParam1, String extParam2, String extParam3) {
        BiRelation biRelation = BiRelation.builder()
                .bizType(bizType)
                .sourceId(sourceId)
                .targetId(targetId)
                .relation(relation)
                .state(state)
                .extParam1(nullToEmpty(extParam1))
                .extParam2(nullToEmpty(extParam2))
                .extParam3(nullToEmpty(extParam3))
                .build();
        return biRelationRepository.save(biRelation);
    }


    /**
     * 基于 upsert 机制新增单个双向关系。
     *
     * @param biRelation 双向关系
     * @return upsert 操作是否成功的 Mono 对象
     */
    public Mono<Boolean> upsert(BiRelation biRelation) {
        // bizType sourceId targetId compose the unique key, so these fields are checked
        Preconditions.checkArgument(isNotBlank(biRelation.getSourceId()));
        Preconditions.checkArgument(isNotBlank(biRelation.getTargetId()));
        Preconditions.checkArgument(biRelation.getBizType() != null);

        Criteria criteria = buildCriteria(biRelation.getSourceId(), biRelation.getTargetId(), biRelation.getBizType());
        return mongoUpsertHelper.upsert(biRelation, criteria);
    }

    // 以下是其他方法

    /**
     * 根据多个源 ID 查询双向关系。
     *
     * @param bizType 业务类型
     * @param sourceIds 源 ID 集合
     * @return 双向关系的 Flux 对象
     */
    public Flux<BiRelation> getBySourceIds(BiRelationBizType bizType, Collection<String> sourceIds) {
        return biRelationRepository.findByBizTypeAndSourceIdIn(bizType, sourceIds);
    }

    // 以下是其他方法

    /**
     * 根据源 ID 查询双向关系。
     *
     * @param bizType 业务类型
     * @param sourceId 源 ID
     * @return 双向关系的 Flux 对象
     */
    public Flux<BiRelation> getBySourceId(BiRelationBizType bizType, String sourceId) {
        return biRelationRepository.findByBizTypeAndSourceId(bizType, sourceId);
    }

    // 以下是其他方法

    /**
     * 根据源 ID 和分页信息查询双向关系。
     *
     * @param bizType 业务类型
     * @param sourceId 源 ID
     * @param pageable 分页信息
     * @return 双向关系的 Flux 对象
     */
    public Flux<BiRelation> getBySourceId(BiRelationBizType bizType, String sourceId, Pageable pageable) {
        return biRelationRepository.findByBizTypeAndSourceId(bizType, sourceId, pageable);
    }

    /**
     * 根据目标标识符集合查询双向关联关系。
     *
     * @param bizType   业务类型。
     * @param targetId  目标标识符集合。
     * @return 表示查询结果的 {@code Flux<BiRelation>} 对象。
     */
    public Flux<BiRelation> getByTargetIds(BiRelationBizType bizType, Collection<String> targetId) {
        return biRelationRepository.findByBizTypeAndTargetIdIn(bizType, targetId);
    }


    /**
     * 根据目标 ID 查询双向关系。
     *
     * @param bizType 业务类型
     * @param targetId 目标 ID
     * @return 双向关系的 Flux 对象
     */
    public Flux<BiRelation> getByTargetId(BiRelationBizType bizType, String targetId) {
        return biRelationRepository.findByBizTypeAndTargetId(bizType, targetId);
    }

    // 以下是其他方法

    // 以下是其他方法

    /**
     * 更新双向关系的关系。
     *
     * @param bizType 业务类型
     * @param sourceId 源 ID
     * @param targetId 目标 ID
     * @param newRelation 新的关系
     * @return 更新操作是否成功的 Mono 对象
     */
    public Mono<Boolean> updateRelation(BiRelationBizType bizType, String sourceId, String targetId,
            String newRelation) {
        Query query = buildQuery(sourceId, targetId, bizType);
        return mongoUpsertHelper.update(BiRelation
                        .builder()
                        .relation(newRelation)
                        .build(),
                query);
    }

    /**
     * 删除单个双向关系。
     *
     * @param bizType 业务类型
     * @param sourceId 源 ID
     * @param targetId 目标 ID
     * @return 删除操作是否成功的 Mono 对象
     */
    public Mono<Boolean> removeBiRelation(BiRelationBizType bizType, String sourceId, String targetId) {
        Query query = buildQuery(sourceId, targetId, bizType);
        return mongoUpsertHelper.remove(query, BiRelation.class);
    }

    // 以下是其他方法

    // 以下是其他方法

    /**
     * 删除所有与指定源 ID 相关的双向关系。
     *
     * @param bizType 业务类型
     * @param sourceId 源 ID
     * @return 删除操作是否成功的 Mono 对象
     */
    public Mono<Boolean> removeAllBiRelations(BiRelationBizType bizType, String sourceId) {
        Query query = new Query();
        query.addCriteria(where(BIZ_TYPE).is(bizType));
        query.addCriteria(where(SOURCE_ID).is(sourceId));
        return mongoUpsertHelper.remove(query, BiRelation.class);
    }

    /**
     * 删除所有与指定目标 ID 相关的双向关系。
     *
     * @param bizType 业务类型
     * @param targetId 目标 ID
     * @return 删除操作是否成功的 Mono 对象
     */
    public Mono<Boolean> removeAllBiRelationsByTargetId(BiRelationBizType bizType, String targetId) {
        Query query = new Query();
        query.addCriteria(where(BIZ_TYPE).is(bizType));
        query.addCriteria(where(TARGET_ID).is(targetId));
        return mongoUpsertHelper.remove(query, BiRelation.class);
    }

    // 以下是其他方法

    // 以下是其他方法

    /**
     * 删除所有与指定源 ID 集合相关的双向关系。
     *
     * @param bizType 业务类型
     * @param sourceIds 源 ID 集合
     * @return 删除操作是否成功的 Mono 对象
     */
    public Mono<Boolean> removeAllBiRelations(BiRelationBizType bizType, List<String> sourceIds) {
        Query query = new Query();
        query.addCriteria(where(BIZ_TYPE).is(bizType));
        query.addCriteria(where(SOURCE_ID).in(sourceIds));
        return mongoUpsertHelper.remove(query, BiRelation.class);
    }

    // 以下是其他方法

    /**
     * 根据源 ID 和目标 ID 查询双向关系。
     *
     * @param bizType 业务类型
     * @param sourceId 源 ID
     * @param targetId 目标 ID
     * @return 双向关系的 Mono 对象
     */
    public Mono<BiRelation> getBiRelation(BiRelationBizType bizType, String sourceId, String targetId) {
        return biRelationRepository.findByBizTypeAndSourceIdAndTargetId(bizType, sourceId, targetId);
    }

    // 以下是其他方法

    /**
     * 根据目标 ID 和多个源 ID 查询双向关系。
     *
     * @param bizType 业务类型
     * @param targetId 目标 ID
     * @param sourceIds 源 ID 集合
     * @return 双向关系的 Flux 对象
     */
    public Flux<BiRelation> getByTargetIdAndSourceIds(BiRelationBizType bizType, String targetId,
            Collection<String> sourceIds) {
        return biRelationRepository.findByBizTypeAndTargetIdAndSourceIdIn(bizType, targetId,
                sourceIds);
    }

    // 以下是其他方法

    /**
     * 更新双向关系的状态。
     *
     * @param bizType 业务类型
     * @param sourceId 源 ID
     * @param targetId 目标 ID
     * @param newState 新的状态
     * @return 更新操作是否成功的 Mono 对象
     */
    public Mono<Boolean> updateState(BiRelationBizType bizType, String sourceId, String targetId,
            String newState) {
        Query query = buildQuery(sourceId, targetId, bizType);
        return mongoUpsertHelper.update(BiRelation.builder()
                        .state(newState)
                        .build(),
                query);
    }

    // 以下是其他方法

    /**
     * 根据源 ID 和关系查询双向关系。
     *
     * @param bizType 业务类型
     * @param sourceId 源 ID
     * @param relation 关系
     * @return 双向关系的 Flux 对象
     */
    public Flux<BiRelation> getBySourceIdAndRelation(BiRelationBizType bizType, String sourceId, String relation) {
        Query query = new Query();
        query.addCriteria(where(BIZ_TYPE).is(bizType));
        query.addCriteria(where(SOURCE_ID).is(sourceId));
        query.addCriteria(where(RELATION).is(relation));

        return biRelationRepository.findByBizTypeAndSourceIdAndRelation(bizType, sourceId, relation);
    }

    // 以下是其他方法

    /**
     * 统计与指定源 ID 相关的双向关系的数量。
     *
     * @param bizType 业务类型
     * @param sourceId 源 ID
     * @return 双向关系的数量的 Mono 对象
     */
    public Mono<Long> countBySourceId(BiRelationBizType bizType, String sourceId) {
        return biRelationRepository.countByBizTypeAndSourceId(bizType, sourceId);
    }

    // 以下是其他方法

    /**
     * 统计与指定目标 ID 相关的双向关系的数量。
     *
     * @param bizType 业务类型
     * @param targetId 目标 ID
     * @return 双向关系的数量的 Mono 对象
     */
    public Mono<Long> countByTargetId(BiRelationBizType bizType, String targetId) {
        return biRelationRepository.countByBizTypeAndTargetId(bizType, targetId);
    }

    /**
     * 构建查询条件。
     *
     * @param sourceId 源标识符。
     * @param targetId 目标标识符。
     * @param bizType 业务类型。
     * @return 表示查询条件的 {@code Criteria} 对象。
     */
    private Criteria buildCriteria(String sourceId, String targetId, BiRelationBizType bizType) {
        return where(BIZ_TYPE).is(bizType)
                .and(SOURCE_ID).is(sourceId)
                .and(TARGET_ID).is(targetId);
    }

    /**
     * 构建查询对象。
     *
     * @param sourceId 源标识符。
     * @param targetId 目标标识符。
     * @param bizType 业务类型。
     * @return 表示查询条件的 {@code Query} 对象。
     */
    private Query buildQuery(String sourceId, String targetId, BiRelationBizType bizType) {
        return new Query(buildCriteria(sourceId, targetId, bizType));
    }


    /**
     * 根据 ID 查询双向关系。
     *
     * @param id 双向关系的 ID
     * @return 双向关系的 Mono 对象
     */
    public Mono<BiRelation> getById(String id) {
        return biRelationRepository.findById(id);
    }

    // 以下是其他方法

    /**
     * 根据 ID 删除双向关系。
     *
     * @param id 双向关系的 ID
     * @return 删除操作是否成功的 Mono 对象
     */
    public Mono<Boolean> removeBiRelationById(String id) {
        return biRelationRepository.deleteById(id)
                .thenReturn(true)
                .onErrorReturn(false);
    }
}
