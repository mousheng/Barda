package com.barda.api.usermanagement.view;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.barda.domain.user.model.Connection;

import lombok.Builder;
import lombok.Getter;

/**
 * 用户个人资料视图类。
 * 该类使用 Lombok 注解来生成构建器和 getter。
 */
@Builder
@Getter
public class UserProfileView {

    /**
     * 用户 ID。
     */
    private String id;

    /**
     * 组织和访客角色列表。
     */
    private List<OrgAndVisitorRoleView> orgAndRoles;

    /**
     * 当前组织 ID。
     */
    private String currentOrgId;

    /**
     * 用户名。
     */
    private String username;

    /**
     * 连接列表。
     */
    private Set<Connection> connections;

    /**
     * 是否为匿名用户。
     */
    @JsonProperty(value = "isAnonymous")
    private boolean isAnonymous;

    /**
     * 是否已启用。
     */
    @JsonProperty(value = "isEnabled")
    private boolean isEnabled;

    /**
     * 用户头像。
     */
    private String avatar;

    /**
     * 用户头像 URL。
     */
    private String avatarUrl;

    /**
     * 是否已设置密码。
     */
    private boolean hasPassword;

    /**
     * 是否已设置昵称。
     */
    private boolean hasSetNickname;

    /**
     * 是否已显示新用户指南。
     */
    private boolean hasShownNewUserGuidance;

    /**
     * 用户状态。
     */
    private Map<String, Object> userStatus;

    /**
     * 是否为组织开发者。
     */
    private boolean isOrgDev;

    /**
     * 创建时间（毫秒）。
     */
    private long createdTimeMs;

    /**
     * IP 地址。
     */
    private String ip;
}
