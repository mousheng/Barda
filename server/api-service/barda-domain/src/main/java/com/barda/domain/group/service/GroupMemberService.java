package com.barda.domain.group.service;

import java.util.Collection;
import java.util.List;

import com.barda.domain.group.model.Group;
import com.barda.domain.group.model.GroupMember;
import com.barda.domain.organization.model.MemberRole;
import com.barda.infra.birelation.BiRelation;

import reactor.core.publisher.Mono;

/**
 * 组成员服务接口。
 * 它提供了一系列方法来操作和管理组成员。
 */
public interface GroupMemberService {

    /**
     * 获取指定组的组成员列表。
     *
     * @param groupId 组ID
     * @param page 页码
     * @param count 每页的记录数
     * @return 组成员列表的Mono
     */
    Mono<List<GroupMember>> getGroupMembers(String groupId, int page, int count);

    /**
     * 向指定组添加组成员。
     *
     * @param orgId 组织ID
     * @param groupId 组ID
     * @param userId 用户ID
     * @param memberRole 组成员的角色
     * @return true表示添加成功，false表示添加失败的Mono
     */
    Mono<Boolean> addMember(String orgId, String groupId, String userId, MemberRole memberRole);

    /**
     * 更新指定组成员的角色。
     *
     * @param groupId 组ID
     * @param userId 用户ID
     * @param memberRole 组成员的新角色
     * @return true表示更新成功，false表示更新失败的Mono
     */
    Mono<Boolean> updateMemberRole(String groupId, String userId, MemberRole memberRole);

    /**
     * 从指定组中删除组成员。
     *
     * @param groupId 组ID
     * @param userId 用户ID
     * @return true表示删除成功，false表示删除失败的Mono
     */
    Mono<Boolean> removeMember(String groupId, String userId);

    /**
     * 获取指定组织中指定用户所属的组ID列表。
     *
     * @param orgId 组织ID
     * @param userId 用户ID
     * @return 组ID列表的Mono
     */
    Mono<List<String>> getUserGroupIdsInOrg(String orgId, String userId);

    /**
     * 获取指定组织中指定用户所属的非动态组ID列表。
     *
     * @param orgId 组织ID
     * @param userId 用户ID
     * @return 非动态组ID列表的Mono
     */
    Mono<List<String>> getNonDynamicUserGroupIdsInOrg(String orgId, String userId);

    /**
     * 获取指定组织中指定用户所属的组成员列表。
     *
     * @param orgId 组织ID
     * @param userId 用户ID
     * @return 组成员列表的Mono
     */
    Mono<List<GroupMember>> getUserGroupMembersInOrg(String orgId, String userId);

    /**
     * 获取指定组中指定用户的组成员信息。
     *
     * @param groupId 组ID
     * @param userId 用户ID
     * @return 组成员信息的Mono
     */
    Mono<GroupMember> getGroupMember(String groupId, String userId);

    /**
     * 获取指定组中所有管理员的组成员列表。
     *
     * @param groupId 组ID
     * @return 组成员列表的Mono
     */
    Mono<List<GroupMember>> getAllGroupAdmin(String groupId);

    /**
     * 从指定组中删除所有组成员。
     *
     * @param groupId 组ID
     * @return true表示删除成功，false表示删除失败的Mono
     */
    Mono<Boolean> deleteGroupMembers(String groupId);

    /**
     * 判断指定用户是否为指定组的组成员。
     *
     * @param group 组
     * @param userId 用户ID
     * @return true表示是组成员，false表示不是组成员的Mono
     */
    Mono<Boolean> isMember(Group group, String userId);

    /**
     * 批量添加组成员。
     *
     * @param groupMembers 组成员列表
     * @return 组成员列表的Mono
     */
    Mono<List<GroupMember>> bulkAddMember(Collection<GroupMember> groupMembers);

    /**
     * 从指定组中批量删除组成员。
     *
     * @param groupId 组ID
     * @param userIds 用户ID列表
     * @return true表示删除成功，false表示删除失败的Mono
     */
    Mono<Boolean> bulkRemoveMember(String groupId, Collection<String> userIds);
}
