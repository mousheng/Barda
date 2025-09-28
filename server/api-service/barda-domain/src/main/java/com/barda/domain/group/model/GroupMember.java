package com.barda.domain.group.model;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.barda.domain.organization.model.MemberRole;
import com.barda.infra.birelation.BiRelation;

/**
 * 该类表示组成员。
 * 它包含组ID、用户ID、角色、组织ID和加入时间等信息。
 * 它提供了一系列方法来操作和检查组成员的状态。
 */
public class GroupMember {

    /**
     * 组ID
     */
    private final String groupId;

    /**
     * 用户ID
     */
    private final String userId;

    /**
     * 组成员的角色
     */
    private final MemberRole role;

    /**
     * 组织ID
     */
    private final String orgId;

    /**
     * 加入时间
     */
    private final long joinTime;

    /**
     * 一个特殊的GroupMember实例，表示组成员不存在。
     */
    public static final GroupMember NOT_EXIST = new GroupMember("", "", MemberRole.MEMBER, "", 0);

    /**
     * 构造函数
     *
     * @param groupId 组ID
     * @param userId 用户ID
     * @param role 组成员的角色
     * @param orgId 组织ID
     * @param joinTime 加入时间
     */
    @JsonCreator
    public GroupMember(String groupId, String userId, MemberRole role, String orgId, long joinTime) {
        this.groupId = groupId;
        this.userId = userId;
        this.role = role;
        this.orgId = orgId;
        this.joinTime = joinTime;
    }

    /**
     * 从BiRelation中创建一个GroupMember实例
     *
     * @param biRelation BiRelation实例
     * @return 创建的GroupMember实例
     */
    public static GroupMember from(BiRelation biRelation) {
        return new GroupMember(biRelation.getSourceId(), biRelation.getTargetId(),
                MemberRole.fromValue(biRelation.getRelation()), biRelation.getExtParam1(),
                biRelation.getCreateTime());
    }

    /**
     * 判断该组成员是否为管理员
     *
     * @return true表示是管理员，false表示不是
     */
    public boolean isAdmin() {
        return role == MemberRole.ADMIN;
    }

    /**
     * 判断该组成员是否为无效的
     *
     * @return true表示是无效的，false表示不是
     */
    @JsonIgnore
    public boolean isInvalid() {
        return this == NOT_EXIST || StringUtils.isBlank(groupId);
    }

    /**
     * 判断该组成员是否为有效的
     *
     * @return true表示是有效的，false表示不是
     */
    @JsonIgnore
    public boolean isValid() {
        return !isInvalid();
    }

    /**
     * 获取组织ID
     *
     * @return 组织ID
     */
    public String getOrgId() {
        return orgId;
    }

    /**
     * 获取组ID
     *
     * @return 组ID
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * 获取用户ID
     *
     * @return 用户ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * 获取组成员的角色
     *
     * @return 组成员的角色
     */
    public MemberRole getRole() {
        return role;
    }

    /**
     * 获取加入时间
     *
     * @return 加入时间
     */
    public long getJoinTime() {
        return joinTime;
    }
}
