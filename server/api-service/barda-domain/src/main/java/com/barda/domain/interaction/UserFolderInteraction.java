package com.barda.domain.interaction;

import static com.barda.infra.birelation.BiRelationBizType.USER_FOLDER_INTERACTION;

import java.time.Instant;

import org.apache.commons.lang3.math.NumberUtils;

import com.google.common.base.Preconditions;
import com.barda.infra.birelation.BiRelation;

/**
 * 该类表示用户与文件夹的交互记录。
 */
public record UserFolderInteraction(String userId, String folderId, Instant lastViewTime) {

    /**
     * 将当前的用户文件夹交互记录转换为双向关系。
     *
     * @return 双向关系
     */
    public BiRelation toBiRelation() {
        return BiRelation.builder()
                .bizType(USER_FOLDER_INTERACTION)
                .sourceId(userId)
                .targetId(folderId)
                .extParam1(lastViewTime.toEpochMilli() + "")
                .build();
    }

    /**
     * 从双向关系中创建一个新的用户文件夹交互记录。
     *
     * @param biRelation 双向关系
     * @return 新的用户文件夹交互记录
     * @throws IllegalArgumentException 如果双向关系的业务类型不是 USER_FOLDER_INTERACTION
     */
    public static UserFolderInteraction of(BiRelation biRelation) {
        Preconditions.checkArgument(biRelation.getBizType() == USER_FOLDER_INTERACTION);
        return new UserFolderInteraction(biRelation.getSourceId(),
                biRelation.getTargetId(),
                Instant.ofEpochMilli(NumberUtils.toLong(biRelation.getExtParam1())));
    }
}
