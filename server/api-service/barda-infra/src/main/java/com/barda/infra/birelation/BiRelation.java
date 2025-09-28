package com.barda.infra.birelation;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.base.MoreObjects;
import com.barda.sdk.models.HasIdAndAuditing;

import lombok.Builder;

/**
 * 双向关系的领域模型。
 *
 * 该类使用 Lombok 的 @Builder 注解来生成构建器模式的构造函数，
 * 并使用 @Document 注解来标记该类可以作为 MongoDB 文档存储。
 *
 */
@Builder
@Document
public class BiRelation extends HasIdAndAuditing {

    /**
     * 业务类型。
     */
    private final BiRelationBizType bizType;

    /**
     * 源 ID。
     */
    private final String sourceId;

    /**
     * 目标 ID。
     */
    private final String targetId;

    /**
     * 关系。
     */
    private final String relation;

    /**
     * 状态。
     */
    private final String state;

    /**
     * 扩展参数 1。
     */
    private final String extParam1;

    /**
     * 扩展参数 2。
     */
    private final String extParam2;

    /**
     * 扩展参数 3。
     */
    private final String extParam3;

    /**
     * 构造函数。
     *
     * @param bizType 业务类型
     * @param sourceId 源 ID
     * @param targetId 目标 ID
     * @param relation 关系
     * @param state 状态
     * @param extParam1 扩展参数 1
     * @param extParam2 扩展参数 2
     * @param extParam3 扩展参数 3
     */
    @JsonCreator
    public BiRelation(BiRelationBizType bizType,
            String sourceId,
            String targetId,
            String relation,
            String state,
            String extParam1,
            String extParam2,
            String extParam3) {
        this.bizType = bizType;
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.relation = relation;
        this.state = state;
        this.extParam1 = extParam1;
        this.extParam2 = extParam2;
        this.extParam3 = extParam3;
    }

    // 以下是 getter 方法

    public BiRelationBizType getBizType() {
        return bizType;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getTargetId() {
        return targetId;
    }

    public String getRelation() {
        return relation;
    }

    public String getState() {
        return state;
    }

    public String getExtParam1() {
        return extParam1;
    }

    public String getExtParam2() {
        return extParam2;
    }

    public String getExtParam3() {
        return extParam3;
    }

    /**
     * 重写 toString 方法，返回对象的字符串表示形式。
     *
     * @return 对象的字符串表示形式
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("bizType", bizType)
                .add("sourceId", sourceId)
                .add("targetId", targetId)
                .add("relation", relation)
                .add("state", state)
                .add("extParam1", extParam1)
                .add("extParam2", extParam2)
                .add("extParam3", extParam3)
                .toString();
    }

    /**
     * 获取创建时间的 Unix 时间戳（毫秒）。
     *
     * @return 创建时间的 Unix 时间戳（毫秒）
     */
    public long getCreateTime() {
        return createdAt != null ? createdAt.toEpochMilli() : 0;
    }
}
