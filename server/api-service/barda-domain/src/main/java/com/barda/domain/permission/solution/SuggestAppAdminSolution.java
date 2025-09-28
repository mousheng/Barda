package com.barda.domain.permission.solution;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.barda.domain.group.model.GroupMember;
import com.barda.domain.group.service.GroupMemberService;
import com.barda.domain.permission.model.ResourcePermission;
import com.barda.domain.permission.model.ResourceRole;
import com.barda.domain.permission.service.ResourcePermissionService;
import com.barda.domain.user.model.User;
import com.barda.domain.user.service.UserService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 提供建议的应用程序管理员解决方案。
 */
@Service
public class SuggestAppAdminSolution {

    /**
     * 显示管理员名称的限制数量。
     */
    private static final int LIMIT_COUNT_FOR_DISPLAY_ADMIN_NAMES = 7;

    /**
     * 用户组成员服务。
     */
    @Autowired
    private GroupMemberService groupMemberService;

    /**
     * 用户服务。
     */
    @Autowired
    private UserService userService;

    /**
     * 资源权限服务。
     */
    @Autowired
    private ResourcePermissionService resourcePermissionService;

    /**
     * 获取应用程序的管理员用户列表。
     *
     * @param applicationId 应用程序ID
     * @param limit 获取用户数量的限制
     * @return 应用程序管理员用户列表的Mono
     */
    public Mono<List<User>> getApplicationAdminUsers(String applicationId, int limit) {
        return resourcePermissionService.getByApplicationId(applicationId)
                .flatMap(permissions -> getSuggestAdminIds(limit, permissions))
                .flatMap(userIds -> userService.getByIds(userIds)
                        .map(mapData -> userIds.stream()
                                .map(mapData::get)
                                .filter(Objects::nonNull)
                                .toList()
                        )
                );
    }

    /**
     * 获取建议的管理员用户ID列表。
     *
     * @param limit 获取用户数量的限制
     * @param permissions 资源权限列表
     * @return 建议的管理员用户ID列表的Mono
     */
    @Nonnull
    private Mono<List<String>> getSuggestAdminIds(int limit, List<ResourcePermission> permissions) {
        List<String> adminUserIds = permissions.stream()
                .filter(it -> it.ownedByUser() && it.getResourceRole() == ResourceRole.OWNER)
                .map(ResourcePermission::getResourceHolderId)
                .toList();
        List<String> adminGroupIds = permissions.stream()
                .filter(it -> it.ownedByGroup() && it.getResourceRole() == ResourceRole.OWNER)
                .map(ResourcePermission::getResourceHolderId)
                .toList();

        if (adminUserIds.size() >= limit) {
            return Mono.just(adminUserIds.stream()
                    .limit(limit)
                    .toList());
        }

        Set<String> adminUserIdSet = newHashSet(adminUserIds);
        return Flux.fromIterable(adminGroupIds)
                .flatMap(groupId -> groupMemberService.getGroupMembers(groupId, 1, 100))
                .flatMapIterable(list -> list)
                .map(GroupMember::getUserId)
                .filter(it -> !adminUserIdSet.contains(it))
                .take(limit - adminUserIds.size())
                .collectList()
                .map(groupUserIds -> {
                    List<String> userIds = newArrayList();
                    userIds.addAll(adminUserIds);
                    userIds.addAll(groupUserIds);
                    return userIds;
                });
    }

    /**
     * 获取建议的应用程序管理员名称。
     *
     * @param applicationId 应用程序ID
     * @return 建议的应用程序管理员名称的Mono
     */
    public Mono<String> getSuggestAppAdminNames(String applicationId) {
        return getApplicationAdminUsers(applicationId, LIMIT_COUNT_FOR_DISPLAY_ADMIN_NAMES)
                .map(users -> users.stream()
                        .map(User::getName)
                        .collect(Collectors.joining(" "))
                );
    }

}

