package com.barda.domain.permission.service;

import static com.barda.sdk.exception.BizError.INVALID_PERMISSION_OPERATION;
import static com.barda.sdk.exception.BizError.NOT_AUTHORIZED;
import static com.barda.sdk.util.ExceptionUtils.ofError;
import static com.barda.sdk.util.ExceptionUtils.ofException;
import static java.util.Collections.singleton;
import static org.apache.commons.collections4.SetUtils.emptyIfNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.barda.domain.permission.model.ResourceAction;
import com.barda.domain.permission.model.ResourceHolder;
import com.barda.domain.permission.model.ResourcePermission;
import com.barda.domain.permission.model.ResourceRole;
import com.barda.domain.permission.model.ResourceType;
import com.barda.domain.permission.model.UserPermissionOnResourceStatus;
import com.barda.infra.annotation.NonEmptyMono;
import com.barda.infra.annotation.PossibleEmptyMono;
import com.barda.sdk.exception.BizException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 资源权限服务类。
 * 它提供了一组方法来操作和查询资源权限。
 */
@Service
public class ResourcePermissionService {

    @Autowired
    private ResourcePermissionRepository repository;

    @Lazy
    @Autowired
    private ApplicationPermissionHandler applicationPermissionHandler;

    @Lazy
    @Autowired
    private DatasourcePermissionHandler datasourcePermissionHandler;

    /**
     * 根据资源类型和资源 ID 获取资源权限。
     *
     * @param resourceType 资源类型
     * @param resourceIds 资源 ID 集合
     * @return 包含资源权限的 Map，键为资源 ID，值为权限列表
     */
    public Mono<Map<String, Collection<ResourcePermission>>> getByResourceTypeAndResourceIds(ResourceType resourceType,
            Collection<String> resourceIds) {
        return repository.getByResourceTypeAndResourceIds(resourceType, resourceIds);
    }

    /**
     * 根据资源类型和资源 ID 获取单个资源权限。
     *
     * @param resourceType 资源类型
     * @param resourceId 资源 ID
     * @return 单个资源权限
     */
    @NonEmptyMono
    public Mono<List<ResourcePermission>> getByResourceTypeAndResourceId(ResourceType resourceType, String resourceId) {
        return repository.getByResourceTypeAndResourceId(resourceType, resourceId);
    }

    /**
     * 根据应用 ID 获取应用的资源权限。
     *
     * @param applicationId 应用 ID
     * @return 应用的资源权限
     */
    @NonEmptyMono
    public Mono<List<ResourcePermission>> getByApplicationId(String applicationId) {
        return getByResourceTypeAndResourceId(ResourceType.APPLICATION, applicationId);
    }

    /**
     * 根据数据源 ID 获取数据源的资源权限。
     *
     * @param dataSourceId 数据源 ID
     * @return 数据源的资源权限
     */
    @NonEmptyMono
    public Mono<List<ResourcePermission>> getByDataSourceId(String dataSourceId) {
        return getByResourceTypeAndResourceId(ResourceType.DATASOURCE, dataSourceId);
    }

    /**
     * 批量插入资源权限。
     *
     * @param resourceType 资源类型
     * @param resourceId 资源 ID
     * @param userIds 用户 ID 集合
     * @param groupIds 组 ID 集合
     * @param role 资源角色
     * @return 插入操作的 Mono
     */
    public Mono<Void> insertBatchPermission(ResourceType resourceType, String resourceId, @Nullable Set<String> userIds,
            @Nullable Set<String> groupIds, ResourceRole role) {
        if (CollectionUtils.isEmpty(userIds) && CollectionUtils.isEmpty(groupIds)) {
            return Mono.empty();
        }
        return repository.insertBatchPermission(resourceType, resourceId, buildResourceHolders(emptyIfNull(userIds), emptyIfNull(groupIds)), role);
    }

    /**
     * 构建资源持有者的映射。
     *
     * @param userIds 用户 ID 集合
     * @param groupIds 组 ID 集合
     * @return 资源持有者的映射
     */
    private Multimap<ResourceHolder, String> buildResourceHolders(@NotNull Set<String> userIds, @NotNull Set<String> groupIds) {
        HashMultimap<ResourceHolder, String> result = HashMultimap.create();
        for (String userId : userIds) {
            result.put(ResourceHolder.USER, userId);
        }
        for (String groupId : groupIds) {
            result.put(ResourceHolder.GROUP, groupId);
        }
        return result;
    }

    /**
     * 为指定资源添加权限。
     *
     * @param resourceType 资源类型
     * @param resourceId 资源 ID
     * @param holderType 持有者类型
     * @param holderId 持有者 ID
     * @param resourceRole 资源角色
     * @return 插入操作是否成功的 Mono
     */
    @SuppressWarnings("SameParameterValue")
    Mono<Boolean> addPermission(ResourceType resourceType, String resourceId,
            ResourceHolder holderType, String holderId,
            ResourceRole resourceRole) {
        return repository.addPermission(resourceType, resourceId, holderType, holderId, resourceRole);
    }

    /**
     * 为数据源添加对用户的权限。
     *
     * @param dataSourceId 数据源 ID
     * @param userId 用户 ID
     * @param role 资源角色
     * @return 插入操作是否成功的 Mono
     */
    public Mono<Boolean> addDataSourcePermissionToUser(String dataSourceId,
            String userId,
            ResourceRole role) {
        return addPermission(ResourceType.DATASOURCE, dataSourceId, ResourceHolder.USER, userId, role);
    }

    /**
     * 为应用添加对用户的权限。
     *
     * @param applicationId 应用 ID
     * @param userId 用户 ID
     * @param role 资源角色
     * @return 插入操作是否成功的 Mono
     */
    public Mono<Boolean> addApplicationPermissionToUser(String applicationId,
            String userId,
            ResourceRole role) {
        return addPermission(ResourceType.APPLICATION, applicationId, ResourceHolder.USER, userId, role);
    }

    /**
     * 为应用添加对组的权限。
     *
     * @param applicationId 应用 ID
     * @param groupId 组 ID
     * @param role 资源角色
     * @return 插入操作是否成功的 Mono
     */
    public Mono<Boolean> addApplicationPermissionToGroup(String applicationId,
            String groupId,
            ResourceRole role) {
        return addPermission(ResourceType.APPLICATION, applicationId, ResourceHolder.GROUP, groupId, role);
    }

    /**
     * 根据权限 ID 获取单个资源权限。
     *
     * @param permissionId 权限 ID
     * @return 单个资源权限
     */
    public Mono<ResourcePermission> getById(String permissionId) {
        return repository.getById(permissionId);
    }

    /**
     * 根据权限 ID 删除资源权限。
     *
     * @param permissionId 权限 ID
     * @return 删除操作是否成功的 Mono
     */
    public Mono<Boolean> removeById(String permissionId) {
        return repository.removePermissionById(permissionId);
    }

    /**
     * 根据权限 ID 更新资源权限的角色。
     *
     * @param permissionId 权限 ID
     * @param role 新的资源角色
     * @return 更新操作是否成功的 Mono
     */
    public Mono<Boolean> updateRoleById(String permissionId, ResourceRole role) {
        return repository.updatePermissionRoleById(permissionId, role);
    }

    /**
     * 获取用户对所有指定资源的权限。
     *
     * @param userId 用户 ID
     * @param resourceIds 资源 ID 集合
     * @param resourceAction 资源操作
     * @return 包含用户对所有指定资源的权限的 Map，键为资源 ID，值为权限列表
     */
    private Mono<Map<String, List<ResourcePermission>>> getAllMatchingPermissions(String userId,
            Collection<String> resourceIds,
            ResourceAction resourceAction) {
        ResourceType resourceType = resourceAction.getResourceType();
        var resourcePermissionHandler = getResourcePermissionHandler(resourceType);
        return resourcePermissionHandler.getAllMatchingPermissions(userId, resourceIds, resourceAction);
    }

    /**
     * 获取资源权限处理器。
     *
     * @param resourceType 资源类型
     * @return 资源权限处理器
     */
    private ResourcePermissionHandler getResourcePermissionHandler(ResourceType resourceType) {
        if (resourceType == ResourceType.DATASOURCE) {
            return datasourcePermissionHandler;
        }

        if (resourceType == ResourceType.APPLICATION) {
            return applicationPermissionHandler;
        }

        throw ofException(INVALID_PERMISSION_OPERATION, "INVALID_PERMISSION_OPERATION", resourceType);
    }

    /**
     * 过滤出用户对指定资源有权限的资源 ID。
     *
     * @param userId 用户 ID
     * @param resourceIds 资源 ID 集合
     * @param resourceAction 资源操作
     * @return 包含用户对指定资源有权限的资源 ID 的 Flux
     */
    public Flux<String> filterResourceWithPermission(String userId, Collection<String> resourceIds, ResourceAction resourceAction) {
        return getAllMatchingPermissions(userId, resourceIds, resourceAction)
                .flatMapIterable(Map::entrySet)
                .filter(entry -> CollectionUtils.isNotEmpty(entry.getValue()))
                .map(Entry::getKey);
    }

    /**
     * 检查用户对指定资源是否有足够的权限，并返回 Mono<Void>。
     *
     * @param userId 用户 ID
     * @param resourceId 资源 ID
     * @param action 资源操作
     * @return Mono<Void>
     */
    public Mono<Void> checkResourcePermissionWithError(String userId, String resourceId, ResourceAction action) {
        return getAllMatchingPermissions(userId, singleton(resourceId), action)
                .flatMap(map -> {
                    List<ResourcePermission> resourcePermissions = map.get(resourceId);
                    if (CollectionUtils.isNotEmpty(resourcePermissions)) {
                        return Mono.empty();
                    }
                    return Mono.error(new BizException(NOT_AUTHORIZED, "NOT_AUTHORIZED"));
                });
    }

    /**
     * 获取用户对指定资源的最高权限。
     *
     * @param userId 用户 ID
     * @param resourceId 资源 ID
     * @param resourceAction 资源操作
     * @return 包含用户对指定资源的最高权限的 Mono
     */
    @PossibleEmptyMono
    public Mono<ResourcePermission> getMaxMatchingPermission(String userId, String resourceId, ResourceAction resourceAction) {
        return getMaxMatchingPermission(userId, Collections.singleton(resourceId), resourceAction)
                .flatMap(map -> {
                    ResourcePermission resourcePermission = map.get(resourceId);
                    if (resourcePermission == null) {
                        return Mono.empty();
                    }
                    return Mono.just(resourcePermission);
                });
    }

    /**
     * 检查用户是否具有对所有资源的足够权限。
     *
     * @param userId 用户ID
     * @param resourceIds 资源ID集合
     * @param resourceAction 资源操作
     * @return 用户是否具有对所有资源的足够权限的Mono
     */
    public Mono<Boolean> haveAllEnoughPermissions(String userId, Collection<String> resourceIds, ResourceAction resourceAction) {
        return getMaxMatchingPermission(userId, resourceIds, resourceAction)
                .map(map -> map.keySet().containsAll(resourceIds));
    }

    /**
     * 获取用户在资源上的最大权限。
     *
     * @param userId 用户ID
     * @param resourceIds 资源ID集合
     * @param resourceAction 资源操作
     * @return 包含资源ID到最大权限映射的Mono
     */
    public Mono<Map<String, ResourcePermission>> getMaxMatchingPermission(String userId,
            Collection<String> resourceIds, ResourceAction resourceAction) {
        return getAllMatchingPermissions(userId, resourceIds, resourceAction)
                .flatMapIterable(Map::entrySet)
                .filter(it -> CollectionUtils.isNotEmpty(it.getValue()))
                .collectMap(Entry::getKey, entry -> getMaxRole(entry.getValue()));
    }

    /**
     * 获取权限列表中的最大权限。
     *
     * @param permissions 权限列表
     * @return 权限列表中的最大权限
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private ResourcePermission getMaxRole(List<ResourcePermission> permissions) {
        return permissions.stream()
                .max(Comparator.comparingInt(it -> it.getResourceRole().getRoleWeight()))
                .get();
    }



    /**
     * 获取用户对指定资源的最高权限。
     *
     * @param userId 用户 ID
     * @param resourceId 资源 ID
     * @param resourceAction 资源操作
     * @return 包含用户对指定资源的最高权限的 Mono
     */
    public Mono<ResourcePermission> checkAndReturnMaxPermission(String userId, String resourceId, ResourceAction resourceAction) {
        return getMaxMatchingPermission(userId, Collections.singleton(resourceId), resourceAction)
                .flatMap(permissionMap -> {
                    if (!permissionMap.containsKey(resourceId)) {
                        return ofError(NOT_AUTHORIZED, "NOT_AUTHORIZED");
                    }
                    return Mono.just(permissionMap.get(resourceId));
                });
    }

    /**
     * 获取用户对应用的权限状态。
     *
     * @param userId 用户 ID
     * @param resourceId 应用 ID
     * @param resourceAction 资源操作
     * @return 用户对应用的权限状态
     */
    public Mono<UserPermissionOnResourceStatus> checkUserPermissionStatusOnResource(String userId, String resourceId, ResourceAction resourceAction) {
        ResourceType resourceType = resourceAction.getResourceType();
        var resourcePermissionHandler = getResourcePermissionHandler(resourceType);
        return resourcePermissionHandler.checkUserPermissionStatusOnResource(userId, resourceId, resourceAction);
    }

    /**
     * 删除用户对应用的权限。
     *
     * @param appId 应用 ID
     * @param userId 用户 ID
     * @return 删除操作是否成功的 Mono
     */
    public Mono<Boolean> removeUserApplicationPermission(String appId, String userId) {
        return repository.removePermissionBy(ResourceType.APPLICATION, appId, ResourceHolder.USER, userId);
    }

    /**
     * 移除用户对数据源的权限。
     *
     * @param appId 应用程序ID
     * @param userId 用户ID
     * @return 是否成功移除用户对数据源权限的Mono
     */
    public Mono<Boolean> removeUserDatasourcePermission(String appId, String userId) {
        return repository.removePermissionBy(ResourceType.APPLICATION, appId, ResourceHolder.USER, userId);
    }

    /**
     * 获取分配给用户的应用程序权限。
     *
     * @param applicationId 应用程序ID
     * @param userId 用户ID
     * @return 分配给用户的应用程序权限的Mono
     */
    public Mono<ResourcePermission> getUserAssignedPermissionForApplication(String applicationId, String userId) {
        return repository.getByResourceTypeAndResourceIdAndTargetId(ResourceType.APPLICATION,
                applicationId, ResourceHolder.USER, userId);
    }
}
