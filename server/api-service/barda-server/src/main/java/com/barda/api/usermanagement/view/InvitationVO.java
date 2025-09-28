package com.barda.api.usermanagement.view;

import com.barda.domain.invitation.model.Invitation;
import com.barda.domain.organization.model.Organization;
import com.barda.domain.user.model.User;

import lombok.Builder;
import lombok.Getter;

/**
 * 邀请视图类。
 * 该类使用 Lombok 的 @Builder 和 @Getter 注解来生成构建器和 getter 方法。
 */
@Builder
@Getter
public class InvitationVO {

    /**
     * 邀请码。
     */
    private final String inviteCode;

    /**
     * 创建者的用户名。
     */
    private final String createUserName;

    /**
     * 被邀请的组织名称。
     */
    private final String invitedOrganizationName;

    /**
     * 从邀请、创建者和被邀请的组织创建邀请视图。
     *
     * @param invitation 邀请
     * @param createUser 创建者
     * @param invitedOrganization 被邀请的组织
     * @return 邀请视图
     */
    public static InvitationVO from(Invitation invitation, User createUser, Organization invitedOrganization) {
        return InvitationVO.builder()
                .inviteCode(invitation.getId())
                .createUserName(createUser.getName())
                .invitedOrganizationName(invitedOrganization.getName())
                .build();
    }
}
