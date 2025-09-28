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
 * 用户应用交互服务类。
 */
@Service
public class UserApplicationInteractionService {

    /**
     * 双向关系服务。
     */
    @Autowired
    private BiRelationService biRelationService;

    /**
     *  upsert（插入或更新）用户应用交互记录。
     *
     * @param userId 用户ID
     * @param applicationId 应用ID
     * @param lastViewTime 最后一次查看应用的时间
     * @return 空的Mono
     */
    public Mono<Void> upsert(String userId, String applicationId, Instant lastViewTime) {
        BiRelation biRelation = new UserApplicationInteraction(userId, applicationId, lastViewTime).toBiRelation();

        return biRelationService.upsert(biRelation).then();
    }

    /**
     * 根据用户ID查找用户应用交互记录。
     *
     * @param userId 用户ID
     * @return 用户应用交互记录的Flux
     */
    public Flux<UserApplicationInteraction> findByUserId(String userId) {
        return biRelationService.getBySourceId(BiRelationBizType.USER_APP_INTERACTION, userId)
                .map(UserApplicationInteraction::of);
    }
}
