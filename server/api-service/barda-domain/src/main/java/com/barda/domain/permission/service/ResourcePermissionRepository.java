package com.barda.domain.permission.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Multimap;
import com.barda.domain.permission.model.ResourceHolder;
import com.barda.domain.permission.model.ResourcePermission;
import com.barda.domain.permission.model.ResourceRole;
import com.barda.domain.permission.model.ResourceType;
import com.barda.infra.annotation.NonEmptyMono;

import reactor.core.publisher.Mono;

/**
 * 资源权限存储库的接口。
 * 它定义了一些公共方法来操作和查询资源权限。
 */
public interface ResourcePermissionRepository {

    /**
     * 根据资源类型和资源 ID 获取资源权限。
     *
     * @param resourceType 资源类型
     * @param resourceIds 资源 ID 集合
     * @return 包含资源权限的 Map，键为资源 ID，值为权限列表
     */
    @NonEmptyMono
    Mono<Map<String, Collection<ResourcePermission>>> getByResourceTypeAndResourceIds(ResourceType resourceType, Collection<String> resourceIds);

    /**
     * 根据资源类型和资源 ID 获取单个资源权限。
     *
     * @param resourceType 资源类型
     * @param resourceId 资源 ID
     * @return 单个资源权限
     */
    @NonEmptyMono
    Mono<List<ResourcePermission>> getByResourceTypeAndResourceId(ResourceType resourceType, String resourceId);

    /**
     * 批量插入资源权限。
     *
     * @param resourceType 资源类型
     * @param resourceId 资源 ID
     * @param resourceHolderMap 资源持有者和持有者 ID 的映射
     * @param role 资源角色
     * @return 插入操作的 Mono
     */
    Mono<Void> insertBatchPermission(ResourceType resourceType, String resourceId,
            Multimap<ResourceHolder, String> resourceHolderMap, ResourceRole role);

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
    Mono<Boolean> addPermission(ResourceType resourceType, String resourceId,
            ResourceHolder holderType, String holderId, ResourceRole resourceRole);

    /**
     * 根据权限 ID 更新资源权限的角色。
     *
     * @param permissionId 权限 ID
     * @param role 新的资源角色
     * @return 更新操作是否成功的 Mono
     */
    Mono<Boolean> updatePermissionRoleById(String permissionId, ResourceRole role);

    /**
     * 根据权限 ID 删除资源权限。
     *
     * @param permissionId 权限 ID
     * @return 删除操作是否成功的 Mono
     */
    Mono<Boolean> removePermissionById(String permissionId);

    /**
     * 根据资源类型、资源 ID、持有者和持有者 ID 删除资源权限。
     *
     * @param resourceType 资源类型
     * @param resourceId 资源 ID
     * @param resourceHolder 持有者类型
     * @param resourceHolderId 持有者 ID
     * @return 删除操作是否成功的 Mono
     */
    Mono<Boolean> removePermissionBy(ResourceType resourceType, String resourceId,
            ResourceHolder resourceHolder,
            String resourceHolderId);

    /**
     * 根据权限 ID 获取单个资源权限。
     *
     * @param permissionId 权限 ID
     * @return 单个资源权限
     */
    Mono<ResourcePermission> getById(String permissionId);

    /**
     * 根据资源类型、资源 ID、持有者类型和持有者 ID 获取单个资源权限。
     *
     * @param resourceType 资源类型
     * @param resourceId 资源 ID
     * @param user 持有者类型
     * @param userId 持有者 ID
     * @return 单个资源权限
     */
    Mono<ResourcePermission> getByResourceTypeAndResourceIdAndTargetId(ResourceType resourceType, String resourceId,
            ResourceHolder user, String userId);
}
