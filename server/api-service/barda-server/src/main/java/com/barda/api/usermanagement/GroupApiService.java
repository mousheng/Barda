package com.barda.api.usermanagement;

import static com.barda.sdk.exception.BizError.CANNOT_LEAVE_GROUP;
import static com.barda.sdk.exception.BizError.CANNOT_REMOVE_MYSELF;
import static com.barda.sdk.exception.BizError.INVALID_GROUP_ID;
import static com.barda.sdk.util.ExceptionUtils.deferredError;
import static com.barda.sdk.util.ExceptionUtils.ofError;
import static com.barda.sdk.util.StreamUtils.collectList;
import static com.barda.sdk.util.StreamUtils.collectMap;
import static java.util.Collections.emptyList;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.barda.api.home.SessionUserService;
import com.barda.api.usermanagement.view.CreateGroupRequest;
import com.barda.api.usermanagement.view.GroupMemberAggregateView;
import com.barda.api.usermanagement.view.GroupMemberView;
import com.barda.api.usermanagement.view.GroupView;
import com.barda.api.usermanagement.view.UpdateGroupRequest;
import com.barda.api.usermanagement.view.UpdateRoleRequest;
import com.barda.api.bizthreshold.AbstractBizThresholdChecker;
import com.barda.domain.group.model.Group;
import com.barda.domain.group.model.GroupMember;
import com.barda.domain.group.service.GroupMemberService;
import com.barda.domain.group.service.GroupService;
import com.barda.domain.organization.model.MemberRole;
import com.barda.domain.organization.model.OrgMember;
import com.barda.domain.user.model.User;
import com.barda.domain.user.service.UserService;
import com.barda.infra.util.TupleUtils;
import com.barda.sdk.exception.BizError;

import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

/**
 * 群组 API 服务类。
 * 该类使用 Spring 注解来声明为服务。
 */
@Service
public class GroupApiService {

    private static final String NOT_AUTHORIZED = "NOT_AUTHORIZED";
    @Autowired
    private SessionUserService sessionUserService;
    @Autowired
    private GroupMemberService groupMemberService;
    @Autowired
    private UserService userService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private AbstractBizThresholdChecker bizThresholdChecker;

    /**
     * 获取群组成员。
     *
     * @param groupId 群组 ID
     * @param page 页码
     * @param count 每页数量
     * @return 群组成员的聚合视图
     */
    public Mono<GroupMemberAggregateView> getGroupMembers(String groupId, int page, int count) {
        Mono<Tuple2<GroupMember, OrgMember>> groupAndOrgMemberInfo = getGroupAndOrgMemberInfo(groupId).cache();

        Mono<MemberRole> visitorRoleMono = groupAndOrgMemberInfo.flatMap(tuple -> {
            GroupMember groupMember = tuple.getT1();
            OrgMember orgMember = tuple.getT2();
            if (groupMember.isAdmin() || orgMember.isAdmin()) {
                return Mono.just(MemberRole.ADMIN);
            }
            if (groupMember.isValid()) {
                return Mono.just(MemberRole.MEMBER);
            }
            return ofError(BizError.NOT_AUTHORIZED, NOT_AUTHORIZED);
        });

        return groupAndOrgMemberInfo
                .filter(this::hasReadPermission)
                .switchIfEmpty(deferredError(BizError.NOT_AUTHORIZED, NOT_AUTHORIZED))
                .flatMap(groupMember -> groupMemberService.getGroupMembers(groupId, page, count))
                .<List<GroupMemberView>> flatMap(members -> {
                    if (members.isEmpty()) {
                        return Mono.just(emptyList());
                    }

                    List<String> userIds = collectList(members, GroupMember::getUserId);
                    Mono<Map<String, User>> userMapMono = userService.getByIds(userIds);
                    return userMapMono.map(map ->
                            members.stream()
                                    .map(orgMember -> {
                                        User user = map.get(orgMember.getUserId());
                                        if (user == null) {
                                            return null;
                                        }
                                        return new GroupMemberView(orgMember, user);
                                    })
                                    .filter(Objects::nonNull)
                                    .toList());
                })
                .zipWith(visitorRoleMono)
                .map(tuple -> {
                    List<GroupMemberView> t1 = tuple.getT1();
                    return GroupMemberAggregateView.builder()
                            .members(t1)
                            .visitorRole(tuple.getT2().getValue())
                            .build();
                });
    }

    /**
     * 检查是否具有读取权限。
     *
     * @param tuple 包含群组成员和组织成员的元组。
     * @return 如果具有读取权限，则返回true，否则返回false。
     */
    private boolean hasReadPermission(Tuple2<GroupMember, OrgMember> tuple) {
        GroupMember groupMember = tuple.getT1();
        OrgMember orgMember = tuple.getT2();
        return groupMember.isValid() || orgMember.isAdmin();
    }

    /**
     * 检查是否具有管理权限。
     *
     * @param tuple 包含群组成员和组织成员的元组。
     * @return 如果具有管理权限，则返回true，否则返回false。
     */
    private boolean hasManagePermission(Tuple2<GroupMember, OrgMember> tuple) {
        GroupMember groupMember = tuple.getT1();
        OrgMember orgMember = tuple.getT2();
        return groupMember.isAdmin() || orgMember.isAdmin();
    }

    /**
     * 获取群组成员和组织成员信息的元组。
     *
     * @param groupId 群组ID。
     * @return 包含群组成员和组织成员信息的Mono。
     */
    private Mono<Tuple2<GroupMember, OrgMember>> getGroupAndOrgMemberInfo(String groupId) {
        // 获取当前访问者的群组成员信息
        Mono<GroupMember> groupMemberMono = sessionUserService.getVisitorId()
                .flatMap(visitorId -> groupMemberService.getGroupMember(groupId, visitorId))
                .defaultIfEmpty(GroupMember.NOT_EXIST);

        // 获取当前访问者的组织成员信息
        Mono<OrgMember> orgMemberMono = sessionUserService.getVisitorOrgMemberCache()
                .flatMap(orgMember -> groupService.getById(groupId)
                        .filter(group -> group.getOrganizationId().equals(orgMember.getOrgId()))
                        .switchIfEmpty(deferredError(INVALID_GROUP_ID, "INVALID_GROUP_ID"))
                        .thenReturn(orgMember)
                )
                .switchIfEmpty(deferredError(INVALID_GROUP_ID, "INVALID_GROUP_ID"));

        // 将群组成员信息和组织成员信息合并成一个元组
        return Mono.zip(groupMemberMono, orgMemberMono);
    }

    /**
     * 添加群组成员。
     *
     * @param groupId   群组ID。
     * @param newUserId 新成员的用户ID。
     * @param roleName  新成员的角色名称。
     * @return 如果成功添加成员，则返回true；否则返回false。
     */
    public Mono<Boolean> addGroupMember(String groupId, String newUserId, String roleName) {
        return getGroupAndOrgMemberInfo(groupId)
                .filter(this::hasManagePermission)
                .switchIfEmpty(deferredError(BizError.NOT_AUTHORIZED, NOT_AUTHORIZED))
                .zipWith(groupService.getById(groupId), TupleUtils::merge)
                .flatMap(tuple -> {
                    String orgId = tuple.getT2().getOrgId();
                    if (tuple.getT3().isDevGroup()) {
                        return bizThresholdChecker.checkMaxDeveloperCount(orgId, groupId, newUserId)
                                .then(groupMemberService.addMember(orgId, groupId, newUserId, MemberRole.fromValue(roleName)));
                    }
                    return groupMemberService.addMember(orgId, groupId, newUserId, MemberRole.fromValue(roleName));
                });
    }

    /**
     * 更新成员角色。
     *
     * @param groupId            群组ID。
     * @param updateRoleRequest  更新角色请求对象。
     * @return 如果成功更新成员角色，则返回true；否则返回false。
     */
    public Mono<Boolean> updateRoleForMember(String groupId, UpdateRoleRequest updateRoleRequest) {
        return getGroupAndOrgMemberInfo(groupId)
                .filter(this::hasManagePermission)
                .switchIfEmpty(deferredError(BizError.NOT_AUTHORIZED, NOT_AUTHORIZED))
                .then(groupMemberService.updateMemberRole(groupId,
                        updateRoleRequest.getUserId(),
                        MemberRole.fromValue(updateRoleRequest.getRole())));
    }

    /**
     * 离开群组。
     *
     * @param groupId 群组ID。
     * @return 如果成功离开群组，则返回true；否则返回false。
     */
    public Mono<Boolean> leaveGroup(String groupId) {
        return Mono.zip(sessionUserService.getVisitorId(), groupMemberService.getAllGroupAdmin(groupId))
                .flatMap(tuple -> {
                    String visitorId = tuple.getT1();
                    List<GroupMember> groupAdmins = tuple.getT2();
                    if (groupAdmins.size() == 1 && groupAdmins.get(0).getUserId().equals(visitorId)) {
                        return ofError(CANNOT_LEAVE_GROUP, "CANNOT_LEAVE_GROUP");
                    }
                    return groupMemberService.removeMember(groupId, visitorId);
                });
    }

    /**
     * 获取群组列表。
     *
     * @return 包含群组视图列表的Mono。
     */
    public Mono<List<GroupView>> getGroups() {

        return sessionUserService.isAnonymousUser()
                .flatMap(isAnonymousUser -> {
                    if (isAnonymousUser) {
                        return Mono.just(emptyList());
                    }

                    return sessionUserService.getVisitorOrgMemberCache()
                            .flatMap(orgMember -> {
                                String orgId = orgMember.getOrgId();
                                if (orgMember.isAdmin()) {
                                    // 如果组织管理员，获取该组织下的所有群组
                                    return groupService.getByOrgId(orgId)
                                            .sort()
                                            .flatMapSequential(group -> GroupView.from(group, MemberRole.ADMIN.getValue()))
                                            .collectList();
                                }
                                // 如果不是管理员，获取用户在组织中的群组信息
                                return groupMemberService.getUserGroupMembersInOrg(orgId, orgMember.getUserId())
                                        .flatMap(groupMembers -> {
                                            List<String> groupIds = collectList(groupMembers, GroupMember::getGroupId);
                                            Map<String, GroupMember> groupMemberMap = collectMap(groupMembers, GroupMember::getGroupId, it -> it);
                                            return groupService.getByIds(groupIds)
                                                    .sort()
                                                    .flatMapSequential(group -> GroupView.from(group,
                                                            groupMemberMap.get(group.getId()).getRole().getValue()))
                                                    .collectList();
                                        });
                            });

                });
    }

    /**
     * 删除群组。
     *
     * @param groupId 群组ID。
     * @return 如果成功删除群组，则返回true；否则返回false。
     */
    public Mono<Boolean> deleteGroup(String groupId) {
        return getGroupAndOrgMemberInfo(groupId)
                .filter(this::hasManagePermission)
                .switchIfEmpty(deferredError(BizError.NOT_AUTHORIZED, NOT_AUTHORIZED))
                .filterWhen(ignored -> groupService.getById(groupId)
                        .map(Group::isNotSystemGroup))
                .switchIfEmpty(deferredError(BizError.CANNOT_DELETE_SYSTEM_GROUP, "CANNOT_DELETE_SYSTEM_GROUP"))
                .then(groupService.delete(groupId)
                        .thenReturn(true)
                );
    }

    /**
     * 创建群组。
     *
     * @param createGroupRequest 创建群组请求对象。
     * @return 包含新创建的群组的Mono。
     */
    public Mono<Group> create(CreateGroupRequest createGroupRequest) {
        return sessionUserService.getVisitorOrgMemberCache()
                .filter(OrgMember::isAdmin)
                .switchIfEmpty(deferredError(BizError.NOT_AUTHORIZED, NOT_AUTHORIZED))
                .delayUntil(orgMember -> bizThresholdChecker.checkMaxGroupCount(orgMember))
                .flatMap(orgMember -> {
                    String orgId = orgMember.getOrgId();
                    Group group = new Group();
                    group.setOrganizationId(orgId);
                    group.setName(createGroupRequest.getName());
                    group.setDynamicRule(createGroupRequest.getDynamicRule());
                    return groupService.create(group, orgMember.getUserId(), orgMember.getOrgId());
                });
    }


    /**
     * 更新群组信息。
     *
     * @param groupId           群组ID。
     * @param updateGroupRequest 更新群组请求对象。
     * @return 如果成功更新群组信息，则返回true；否则返回false。
     */
    public Mono<Boolean> update(String groupId, UpdateGroupRequest updateGroupRequest) {
        return getGroupAndOrgMemberInfo(groupId)
                .filter(this::hasManagePermission)
                .switchIfEmpty(deferredError(BizError.NOT_AUTHORIZED, NOT_AUTHORIZED))
                .flatMap(it -> {
                    Group updateGroup = new Group();
                    updateGroup.setId(groupId);
                    updateGroup.setName(updateGroupRequest.getGroupName());
                    updateGroup.setDynamicRule(updateGroupRequest.getDynamicRule());
                    return groupService.updateGroup(updateGroup);
                });
    }

    /**
     * 从群组中移除用户。
     *
     * @param groupId 群组ID。
     * @param userId  要移除的用户ID。
     * @return 如果成功从群组中移除用户，则返回true；否则返回false。
     */
    public Mono<Boolean> removeUser(String groupId, String userId) {
        return getGroupAndOrgMemberInfo(groupId)
                .filter(this::hasManagePermission)
                .switchIfEmpty(deferredError(BizError.NOT_AUTHORIZED, NOT_AUTHORIZED))
                .flatMap(tuple -> {
                    String currentUserId = tuple.getT2().getUserId();
                    if (currentUserId.equals(userId)) {
                        return ofError(CANNOT_REMOVE_MYSELF, "CANNOT_REMOVE_MYSELF");
                    }
                    return groupMemberService.removeMember(groupId, userId);
                });
    }
}
