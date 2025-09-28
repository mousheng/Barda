package com.barda.domain.invitation.repository;

import com.mongodb.client.result.UpdateResult;

import reactor.core.publisher.Mono;

/**
 * 自定义邀请仓库接口。
 * 该接口定义了与邀请相关的自定义操作。
 */
public interface CustomInvitationRepository {

    /**
     * 向指定的邀请中添加被邀请的用户。
     *
     * @param invitationId 邀请ID
     * @param userId 被邀请的用户ID
     * @return 包含更新结果的Mono
     */
    Mono<UpdateResult> addInvitedUser(String invitationId, String userId);
}
