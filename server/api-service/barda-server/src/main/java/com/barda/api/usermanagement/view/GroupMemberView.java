package com.barda.api.usermanagement.view;

import com.barda.domain.group.model.GroupMember;
import com.barda.domain.user.model.User;

/**
 * 群组成员视图类。
 */
public class GroupMemberView {

    /**
     * 群组成员。
     */
    private final GroupMember groupMember;

    /**
     * 用户。
     */
    private final User user;

    /**
     * 构造器。
     *
     * @param groupMember 群组成员
     * @param user 用户
     */
    public GroupMemberView(GroupMember groupMember, User user) {
        this.groupMember = groupMember;
        this.user = user;
    }

    /**
     * 获取用户 ID。
     *
     * @return 用户 ID
     */
    public String getUserId() {
        return user.getId();
    }

    /**
     * 获取用户名。
     *
     * @return 用户名
     */
    public String getUserName() {
        return user.getName();
    }

    /**
     * 获取头像 URL。
     *
     * @return 头像 URL
     */
    public String getAvatarUrl() {
        return user.getAvatarUrl();
    }

    /**
     * 获取角色。
     *
     * @return 角色
     */
    public String getRole() {
        return groupMember.getRole().getValue();
    }

    /**
     * 获取群组 ID。
     *
     * @return 群组 ID
     */
    public String getGroupId() {
        return groupMember.getGroupId();
    }

    /**
     * 获取组织 ID。
     *
     * @return 组织 ID
     */
    public String getOrgId() {
        return groupMember.getOrgId();
    }

    /**
     * 获取加入时间。
     *
     * @return 加入时间
     */
    public long getJoinTime() {
        return groupMember.getJoinTime();
    }
}
