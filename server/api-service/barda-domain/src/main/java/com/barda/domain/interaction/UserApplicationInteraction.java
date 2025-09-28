package com.barda.domain.interaction;

import java.time.Instant;

import org.apache.commons.lang3.math.NumberUtils;

import com.google.common.base.Preconditions;
import com.barda.infra.birelation.BiRelation;
import com.barda.infra.birelation.BiRelationBizType;

/**
 * 该类表示用户与应用的交互记录。
 */
public record UserApplicationInteraction(String userId, String applicationId, Instant lastViewTime) {

    /**
     * 将当前的用户应用交互记录转换为双向关系。
     *
     * @return 双向关系
     */
    public BiRelation toBiRelation() {
        return BiRelation.builder()
                .bizType(BiRelationBizType.USER_APP_INTERACTION)
                .sourceId(userId)
                .targetId(applicationId)
                .extParam1(lastViewTime.toEpochMilli() + "")
                .build();
    }

    /**
     * 从双向关系中创建一个新的用户应用交互记录。
     *
     * @param biRelation 双向关系
     * @return 新的用户应用交互记录
     * @throws IllegalArgumentException 如果双向关系的业务类型不是 USER_APP_INTERACTION
     */
    public static UserApplicationInteraction of(BiRelation biRelation) {
        Preconditions.checkArgument(biRelation.getBizType() == BiRelationBizType.USER_APP_INTERACTION);
        return new UserApplicationInteraction(biRelation.getSourceId(),
                biRelation.getTargetId(),
                Instant.ofEpochMilli(NumberUtils.toLong(biRelation.getExtParam1())));
    }
}
