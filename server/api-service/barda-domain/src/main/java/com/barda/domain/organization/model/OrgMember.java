package com.barda.domain.organization.model;

import static com.google.common.base.Strings.nullToEmpty;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.barda.infra.birelation.BiRelation;

/**
 * 组织成员类。
 * 该类表示一个组织中的成员，并包含该成员的相关信息。
 */
public class OrgMember {

    /**
     * 组织 ID。
     */
    private final String orgId;

    /**
     * 用户 ID。
     */
    private final String userId;

    /**
     * 成员的角色。
     */
    private final MemberRole role;

    /**
     * 成员的状态。
     */
    private final String state;

    /**
     * 成员加入组织的时间。
     */
    private final long joinTime;

    /**
     * 指示不存在的组织成员。
     */
    public static final OrgMember NOT_EXIST = new OrgMember("", "", MemberRole.MEMBER, "", 0);

    /**
     * 构造函数。
     *
     * @param orgId 组织 ID
     * @param userId 用户 ID
     * @param role 成员的角色
     * @param state 成员的状态
     * @param joinTime 成员加入组织的时间
     */
    @JsonCreator
    public OrgMember(String orgId, String userId, MemberRole role, String state, long joinTime) {
        this.orgId = orgId;
        this.userId = userId;
        this.role = role;
        this.state = state;
        this.joinTime = joinTime;
    }

    /**
     * 判断该组织成员是否无效。
     *
     * @return true - 无效，false - 有效
     */
    @JsonIgnore
    public boolean isInvalid() {
        return this == NOT_EXIST || StringUtils.isBlank(orgId);
    }

    /**
     * 从 BiRelation 对象创建 OrgMember 对象。
     *
     * @param biRelation BiRelation 对象
     * @return 创建的 OrgMember 对象
     */
    public static OrgMember from(BiRelation biRelation) {
        return new OrgMember(biRelation.getSourceId(),
                biRelation.getTargetId(),
                MemberRole.fromValue(biRelation.getRelation()),
                nullToEmpty(biRelation.getState()),
                biRelation.getCreatedAt().toEpochMilli());
    }

    /**
     * 获取组织 ID。
     *
     * @return 组织 ID
     */
    public String getOrgId() {
        return orgId;
    }

    /**
     * 获取用户 ID。
     *
     * @return 用户 ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * 获取成员的角色。
     *
     * @return 成员的角色
     */
    public MemberRole getRole() {
        return role;
    }

    /**
     * 判断该成员是否为管理员。
     *
     * @return true - 是管理员，false - 不是管理员
     */
    public boolean isAdmin() {
        return role == MemberRole.ADMIN;
    }

    /**
     * 判断该成员是否为当前组织的成员。
     *
     * @return true - 是当前组织的成员，false - 不是当前组织的成员
     */
    public boolean isCurrentOrg() {
        return OrgMemberState.CURRENT.getValue().equals(state);
    }

    /**
     * 获取成员加入组织的时间。
     *
     * @return 加入组织的时间（以毫秒为单位）
     */
    public long getJoinTime() {
        return joinTime;
    }
}
