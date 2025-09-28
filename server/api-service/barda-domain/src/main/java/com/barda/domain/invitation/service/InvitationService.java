package com.barda.domain.invitation.service;


import javax.annotation.Nonnull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.barda.domain.invitation.model.Invitation;
import com.barda.domain.invitation.repository.InvitationRepository;
import com.barda.domain.organization.model.MemberRole;
import com.barda.domain.organization.service.OrgMemberService;

import reactor.core.publisher.Mono;

/**
 * 邀请服务类。
 * 该类提供创建、读取、更新和删除邀请的功能，并添加了邀请用户加入组织的功能。
 */
@Lazy
@Service
public class InvitationService {

    /**
     * 邀请仓库。
     */
    @Autowired
    private InvitationRepository invitationRepository;

    /**
     * 组织成员服务。
     */
    @Autowired
    private OrgMemberService orgMemberService;

    /**
     * 邀请仓库。
     * 注意：此处存在冗余的注入，可以考虑将其删除。
     */
    @Autowired
    private InvitationRepository repository;

    /**
     * 创建邀请。
     *
     * @param invitation 邀请
     * @return 包含创建的邀请的Mono
     */
    public Mono<Invitation> create(Invitation invitation) {
        return repository.save(invitation);
    }

    /**
     * 根据邀请ID获取邀请。
     *
     * @param invitationId 邀请ID
     * @return 包含邀请的Mono
     */
    public Mono<Invitation> getById(@Nonnull String invitationId) {
        return invitationRepository.findById(invitationId);
    }

    /**
     * 邀请用户加入组织。
     *
     * @param userId 用户ID
     * @param orgId 组织ID
     * @return 包含邀请结果的Mono
     */
    public Mono<Boolean> inviteToOrg(String userId, String orgId) {
        return orgMemberService.addMember(orgId, userId, MemberRole.MEMBER);
    }
}
