package com.barda.domain.invitation.model;

import java.util.Set;

import org.springframework.data.mongodb.core.mapping.Document;

import com.barda.sdk.models.HasIdAndAuditing;

import lombok.Builder;
import lombok.Getter;

/**
 * 邀请类，用于表示组织邀请的相关信息。
 * 该类使用 Lombok 库中的 @Document、@Builder 和 @Getter 注解来实现。
 */
@Document
@Builder
@Getter
public class Invitation extends HasIdAndAuditing {

    /**
     * 创建此邀请的用户ID。
     */
    private final String createUserId;

    /**
     * 被邀请的组织ID。
     */
    private final String invitedOrganizationId;

    /**
     * 被此邀请的用户ID集合。
     */
    private final Set<String> invitedUserIds;
}
