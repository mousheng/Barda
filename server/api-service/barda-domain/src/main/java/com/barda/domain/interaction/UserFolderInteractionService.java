package com.barda.domain.interaction;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.barda.infra.birelation.BiRelation;
import com.barda.infra.birelation.BiRelationBizType;
import com.barda.infra.birelation.BiRelationService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 用户文件夹交互服务类。
 */
@Service
public class UserFolderInteractionService {

    /**
     * 双向关系服务。
     */
    @Autowired
    private BiRelationService biRelationService;

    /**
     *  upsert（插入或更新）用户文件夹交互记录。
     *
     * @param userId 用户ID
     * @param folderId 文件夹ID
     * @param lastViewTime 最后一次查看文件夹的时间
     * @return 空的Mono
     */
    public Mono<Void> upsert(String userId, String folderId, Instant lastViewTime) {
        BiRelation biRelation = new UserFolderInteraction(userId, folderId, lastViewTime).toBiRelation();
        return biRelationService.upsert(biRelation).then();
    }

    /**
     * 根据用户ID查找用户文件夹交互记录。
     *
     * @param userId 用户ID
     * @return 用户文件夹交互记录的Flux
     */
    public Flux<UserFolderInteraction> findByUserId(String userId) {
        return biRelationService.getBySourceId(BiRelationBizType.USER_FOLDER_INTERACTION, userId)
                .map(UserFolderInteraction::of);
    }
}
