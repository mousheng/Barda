package com.barda.domain.invitation.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.barda.domain.invitation.model.Invitation;

/**
 * 邀请仓库接口。
 * 该接口继承了 {@link ReactiveMongoRepository}，并实现了 {@link CustomInvitationRepository}。
 * 它提供对邀请的 CRUD（创建、读取、更新、删除）操作，并添加了自定义的邀请操作。
 */
public interface InvitationRepository extends ReactiveMongoRepository<Invitation, String>, CustomInvitationRepository {

}
