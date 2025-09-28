package com.barda.domain.permission.service;

import static com.barda.sdk.constants.Authentication.isAnonymousUser;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.barda.domain.group.service.GroupMemberService;
import com.barda.domain.organization.service.OrgMemberService;
import com.barda.domain.permission.model.ResourceAction;
import com.barda.domain.permission.model.ResourceHolder;
import com.barda.domain.permission.model.ResourcePermission;
import com.barda.domain.permission.model.ResourceRole;
import com.barda.domain.permission.model.ResourceType;
import com.barda.domain.permission.model.UserPermissionOnResourceStatus;

import reactor.core.publisher.Mono;

/**
 * 资源权限处理器抽象类，用于处理用户对资源的权限操作。
 */
abstract class ResourcePermissionHandler {

    /**
     * 资源权限服务
     */
    @Autowired
    private ResourcePermissionService resourcePermissionService;

    /**
     * 用户组成员服务
     */
    @Autowired
    private GroupMemberService groupMemberService;

    /**
     * 组织成员服务
     */
    @Autowired
    private OrgMemberService orgMemberService;

    /**
     * 获取所有匹配的权限。
     *
     * @param userId 用户ID
     * @param resourceIds 资源ID集合
     * @param resourceAction 资源操作
     * @return 匹配的权限的Mono，映射为资源ID到权限列表的映射
     */
    public Mono<Map<String, List<ResourcePermission>>> getAllMatchingPermissions(String userId,
            Collection<String> resourceIds,
            ResourceAction resourceAction) {

        ResourceType resourceType = resourceAction.getResourceType();

        if (CollectionUtils.isEmpty(resourceIds)) {
            return Mono.just(emptyMap());
        }

        if (isAnonymousUser(userId)) {
            return getAnonymousUserPermissions(resourceIds, resourceAction);
        }

        return getOrgId(resourceIds.iterator().next())
                .flatMap(orgId -> orgMemberService.getOrgMember(orgId, userId))
                .flatMap(orgMember -> {
                    if (orgMember.isAdmin()) {
                        return Mono.just(buildAdminPermissions(resourceType, resourceIds, userId));
                    }
                    return getAllMatchingPermissions0(userId, orgMember.getOrgId(), resourceType, resourceIds, resourceAction);
                })
                .switchIfEmpty(Mono.just(Maps.newHashMap()))
                .zipWith(getAnonymousUserPermissions(resourceIds, resourceAction))
                .flatMap(tuple2 -> {
                    Map<String, List<ResourcePermission>> permissionMap = tuple2.getT1();
                    Map<String, List<ResourcePermission>> templatePermissionMap = tuple2.getT2();
                    templatePermissionMap.forEach((key, value) -> permissionMap.merge(key, value, ListUtils::union));
                    return Mono.just(permissionMap);
                });
    }

    /**
     * 检查用户在资源上的权限状态。
     *
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @param resourceAction 资源操作
     * @return 用户在资源上的权限状态的Mono
     */
    public Mono<UserPermissionOnResourceStatus> checkUserPermissionStatusOnResource(String userId,
            String resourceId, ResourceAction resourceAction) {

        ResourceType resourceType = resourceAction.getResourceType();

        Mono<UserPermissionOnResourceStatus> publicResourcePermissionMono = getAnonymousUserPermissions(singletonList(resourceId), resourceAction)
                .map(it -> it.getOrDefault(resourceId, emptyList()))
                .map(it -> {
                    if (!it.isEmpty()) {
                        return UserPermissionOnResourceStatus.success(it.get(0));
                    }
                    return isAnonymousUser(userId) ? UserPermissionOnResourceStatus.anonymousUser() : UserPermissionOnResourceStatus.notInOrg();
                });

        if (isAnonymousUser(userId)) {
            return publicResourcePermissionMono;
        }

        Mono<UserPermissionOnResourceStatus> orgUserPermissionMono = getOrgId(resourceId)
                .flatMap(orgId -> orgMemberService.getOrgMember(orgId, userId))
                .flatMap(orgMember -> {
                    if (orgMember.isAdmin()) {
                        return Mono.just(UserPermissionOnResourceStatus.success(buildAdminPermission(resourceType, resourceId, userId)));
                    }
                    return getAllMatchingPermissions0(userId, orgMember.getOrgId(), resourceType, Collections.singleton(resourceId), resourceAction)
                            .map(it -> it.getOrDefault(resourceId, emptyList()))
                            .map(permissions -> permissions.isEmpty() ? UserPermissionOnResourceStatus.notEnoughPermission()
                                                                      : UserPermissionOnResourceStatus.success(getMaxPermission(permissions)));
                })
                .defaultIfEmpty(UserPermissionOnResourceStatus.notInOrg());

        return Mono.zip(publicResourcePermissionMono, orgUserPermissionMono)
                .map(tuple -> {
                    UserPermissionOnResourceStatus publicResourcePermission = tuple.getT1();
                    UserPermissionOnResourceStatus orgUserPermission = tuple.getT2();
                    if (orgUserPermission.hasPermission()) {
                        return orgUserPermission;
                    }
                    if (publicResourcePermission.hasPermission()) {
                        return publicResourcePermission;
                    }
                    return orgUserPermission;
                });
    }

    /**
     * 获取最大的权限。
     *
     * @param permissions 权限列表
     * @return 最大权限
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Nonnull
    private ResourcePermission getMaxPermission(List<ResourcePermission> permissions) {
        return permissions.stream()
                .max(Comparator.comparingInt(it -> it.getResourceRole().getRoleWeight()))
                .get();
    }

    /**
     * 获取匿名用户的权限。
     *
     * @param resourceIds 资源ID集合
     * @param resourceAction 资源操作
     * @return 匿名用户权限的Mono，映射为资源ID到权限列表的映射
     */
    protected abstract Mono<Map<String, List<ResourcePermission>>> getAnonymousUserPermissions(Collection<String> resourceIds,
            ResourceAction resourceAction);

    /**
     * 获取所有匹配的权限。
     *
     * @param userId 用户ID
     * @param orgId 组织ID
     * @param resourceType 资源类型
     * @param resourceIds 资源ID集合
     * @param resourceAction 资源操作
     * @return 匹配的权限的Mono，映射为资源ID到权限列表的映射
     */
    private Mono<Map<String, List<ResourcePermission>>> getAllMatchingPermissions0(String userId, String orgId, ResourceType resourceType,
            Collection<String> resourceIds,
            ResourceAction resourceAction) {
        Mono<Map<String, Collection<ResourcePermission>>> permissionsMapMono =
                resourcePermissionService.getByResourceTypeAndResourceIds(resourceType, resourceIds);
        Mono<Set<String>> userGroupIdsMono = getUserGroupIds(orgId, userId);

        return Mono.zip(userGroupIdsMono, permissionsMapMono)
                .map(tuple -> {
                    Set<String> userGroupIds = tuple.getT1();
                    Map<String, Collection<ResourcePermission>> permissionMap = tuple.getT2();

                    return resourceIds
                            .stream()
                            .collect(toMap(identity(),
                                    resourceId -> {
                                        var resourcePermissions = permissionMap.getOrDefault(resourceId, emptyList());
                                        return filterMatchingPermissions(userId, userGroupIds, resourcePermissions, resourceAction);
                                    }));
                });
    }

    /**
     * 过滤匹配的权限。
     *
     * @param userId 用户ID
     * @param userGroupIds 用户组ID集合
     * @param resourcePermissions 资源权限列表
     * @param resourceAction 资源操作
     * @return 匹配的权限列表
     */
    private List<ResourcePermission> filterMatchingPermissions(String userId,
            Set<String> userGroupIds, Collection<ResourcePermission> resourcePermissions, ResourceAction resourceAction) {
        if (CollectionUtils.isEmpty(resourcePermissions)) {
            return emptyList();
        }
        return resourcePermissions.stream()
                .filter(permission -> permission.matchUser(userId, resourceAction)
                        || permission.matchGroup(userGroupIds, resourceAction))
                .toList();
    }

    /**
     * 构建管理员权限。
     *
     * @param resourceType 资源类型
     * @param resourceIds 资源ID集合
     * @param userId 用户ID
     * @return 管理员权限的映射，映射为资源ID到权限列表的映射
     */
    private Map<String, List<ResourcePermission>> buildAdminPermissions(ResourceType resourceType,
            Collection<String> resourceIds, String userId) {
        return resourceIds.stream()
                .distinct()
                .collect(toMap(it -> it,
                        resourceId -> singletonList(buildAdminPermission(resourceType, userId, resourceId)))
                );
    }

    /**
     * 构建管理员权限。
     *
     * @param resourceType 资源类型
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @return 管理员权限
     */
    private ResourcePermission buildAdminPermission(ResourceType resourceType, String userId, String resourceId) {
        return ResourcePermission.builder()
                .resourceType(resourceType)
                .resourceId(resourceId)
                .resourceHolder(ResourceHolder.USER)
                .resourceHolderId(userId)
                .resourceRole(ResourceRole.OWNER)
                .build();
    }


    /**
     * 获取用户组ID集合。
     *
     * @param orgId 组织ID
     * @param userId 用户ID
     * @return 用户组ID的Mono
     */
    private Mono<Set<String>> getUserGroupIds(String orgId, String userId) {
        return groupMemberService.getUserGroupIdsInOrg(orgId, userId)
                .map(Sets::newHashSet);
    }

    protected abstract Mono<String> getOrgId(String resourceId);
}

