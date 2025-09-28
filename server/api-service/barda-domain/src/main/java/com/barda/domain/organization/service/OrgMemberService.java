package com.barda.domain.organization.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.barda.domain.organization.model.MemberRole;
import com.barda.domain.organization.model.OrgMember;
import com.barda.infra.annotation.PossibleEmptyMono;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 组织成员服务的接口。
 * 该接口提供查询、添加、更新和删除组织成员的功能。
 */
public interface OrgMemberService {

    /**
     * 获取指定组织下的所有组织成员。
     *
     * @param orgId 组织ID
     * @return 组织成员列表
     */
    Flux<OrgMember> getOrganizationMembers(String orgId);

    /**
     * 获取指定组织下的组织成员，并进行分页。
     *
     * @param orgId 组织ID
     * @param page 页码
     * @param count 每页的记录数
     * @return 组织成员列表
     */
    Flux<OrgMember> getOrganizationMembers(String orgId, int page, int count);

    /**
     * 获取指定用户的组织成员。
     *
     * @param userId 用户ID
     * @return 组织成员
     */
    Mono<OrgMember> getCurrentOrgMember(String userId);

    /**
     * 获取指定用户的所有活跃组织。
     *
     * @param userId 用户ID
     * @return 活跃组织列表
     */
    @PossibleEmptyMono
    Flux<OrgMember> getAllActiveOrgs(String userId);

    /**
     * 获取指定组织的组织成员的数量。
     *
     * @param orgId 组织ID
     * @return 组织成员的数量
     */
    Mono<Long> getOrgMemberCount(String orgId);

    /**
     * 获取指定用户的所有活跃组织的数量。
     *
     * @param userId 用户ID
     * @return 活跃组织的数量
     */
    Mono<Long> countAllActiveOrgs(String userId);

    /**
     * 获取指定组织和用户的组织成员。
     *
     * @param orgId 组织ID
     * @param userId 用户ID
     * @return 组织成员
     */
    Mono<OrgMember> getOrgMember(String orgId, String userId);

    /**
     * 添加一个新的组织成员。
     *
     * @param orgId 组织ID
     * @param userId 用户ID
     * @param memberRole 成员角色
     * @return 是否添加成功
     */
    Mono<Boolean> addMember(String orgId, String userId, MemberRole memberRole);

    /**
     * 更新组织成员的角色。
     *
     * @param orgId 组织ID
     * @param userId 用户ID
     * @param memberRole 新成员角色
     * @return 是否更新成功
     */
    Mono<Boolean> updateMemberRole(String orgId, String user, MemberRole memberRole);

    /**
     * 删除指定组织和用户的组织成员。
     *
     * @param orgId 组织ID
     * @param userId 用户ID
     * @return 是否删除成功
     */
    Mono<Boolean> removeMember(String orgId, String userId);

    /**
     * 删除指定组织的所有组织成员。
     *
     * @param orgId 组织ID
     * @return 是否删除成功
     */
    Mono<Boolean> deleteOrgMembers(String orgId);

    /**
     * 尝试添加一个新的组织成员，如果该成员已存在，则不执行任何操作。
     *
     * @param orgId 组织ID
     * @param userId 用户ID
     * @param role 成员角色
     * @return 是否添加成功
     */
    Mono<Boolean> tryAddOrgMember(String orgId, String userId, MemberRole role);

    /**
     * 获取指定组织和用户的组织成员的角色。
     *
     * @param orgIds 组织ID列表
     * @param userId 用户ID
     * @return 组织成员的角色列表
     */
    Mono<Map<String, OrgMember>> getOrgMemberRoles(Collection<String> orgIds, String userId);

    /**
     * 获取指定用户的组织成员信息。
     *
     * @param userId 用户ID
     * @return 用户的组织成员信息
     */
    Mono<UserOrgMemberInfo> getUserOrgMemberInfo(String userId);

    /**
     * 标记指定组织和用户为当前的组织。
     *
     * @param orgId 组织ID
     * @param userId 用户ID
     * @return 是否标记成功
     */
    Mono<Boolean> markAsUserCurrentOrgId(String orgId, String userId);

    /**
     * 移除指定组织和用户的当前组织标记。
     *
     * @param previousCurrentOrgId 之前的当前组织ID
     * @param userId 用户ID
     * @return 是否移除成功
     */
    Mono<Boolean> removeCurrentOrgMark(String previousCurrentOrgId, String userId);

    /**
     * 获取指定组织的所有管理员。
     *
     * @param orgId 组织ID
     * @return 管理员列表
     */
    Mono<List<OrgMember>> getAllOrgAdmins(String orgId);

    /**
     * 批量添加新的组织成员。
     *
     * @param orgId 组织ID
     * @param userIds 用户ID列表
     * @param memberRole 成员角色
     * @return 是否添加成功
     */
    Mono<Void> bulkAddMember(String orgId, Collection<String> userIds, MemberRole memberRole);

    /**
     * 批量删除指定组织和用户的组织成员。
     *
     * @param orgId 组织ID
     * @param userIds 用户ID列表
     * @return 是否删除成功
     */
    Mono<Boolean> bulkRemoveMember(String orgId, Collection<String> userIds);

    /**
     * 记录用户的组织成员信息。
     */
    record UserOrgMemberInfo(OrgMember currentOrgMember, List<OrgMember> orgMembers) {
    }

}
