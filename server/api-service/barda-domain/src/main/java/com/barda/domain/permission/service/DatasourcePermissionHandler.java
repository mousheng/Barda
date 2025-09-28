package com.barda.domain.permission.service;

import static com.barda.domain.permission.model.ResourceHolder.USER;
import static com.barda.sdk.constants.Authentication.ANONYMOUS_USER_ID;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.barda.domain.datasource.model.Datasource;
import com.barda.domain.datasource.service.DatasourceService;
import com.barda.domain.permission.model.ResourceAction;
import com.barda.domain.permission.model.ResourcePermission;
import com.barda.domain.permission.model.ResourceRole;
import com.barda.domain.permission.model.ResourceType;

import reactor.core.publisher.Mono;

/**
 * 数据源权限处理器的实现类，继承自 ResourcePermissionHandler。
 * 它使用懒加载模式创建，并使用 Spring 组件注解。
 */
@Lazy
@Component
class DatasourcePermissionHandler extends ResourcePermissionHandler {

    private static final ResourceRole SYSTEM_STATIC_DATASOURCE_USER_ROLE = ResourceRole.OWNER;

    /**
     * 数据源服务的注入点。
     */
    @Autowired
    private DatasourceService datasourceService;

    /**
     * 获取匿名用户在特定资源上的权限。
     *
     * @param resourceIds 资源 ID 集合
     * @param resourceAction 资源操作
     * @return 包含资源权限的 Map，键为资源 ID，值为权限列表
     */
    @Override
    protected Mono<Map<String, List<ResourcePermission>>> getAnonymousUserPermissions(Collection<String> resourceIds, ResourceAction resourceAction) {
        return Mono.just(Collections.emptyMap());
    }

    /**
     * 获取特定资源的组织 ID。
     *
     * @param resourceId 资源 ID
     * @return 组织 ID 的 Mono
     */
    @Override
    protected Mono<String> getOrgId(String resourceId) {
        return datasourceService.getById(resourceId)
                .map(Datasource::getOrganizationId);
    }

    /**
     * 获取所有匹配的权限。
     *
     * @param userId 用户 ID
     * @param resourceIds 资源 ID 集合
     * @param resourceAction 资源操作
     * @return 包含资源权限的 Map，键为资源 ID，值为权限列表
     */
    @Override
    public Mono<Map<String, List<ResourcePermission>>> getAllMatchingPermissions(String userId, Collection<String> resourceIds,
            ResourceAction resourceAction) {

        List<String> systemStaticDatasourceIds = resourceIds.stream()
                .filter(Datasource::isSystemStaticId)
                .distinct()
                .toList();
        List<String> nonSystemStaticDatasourceIds = resourceIds.stream()
                .filter(Datasource::isNotSystemStaticId)
                .distinct()
                .toList();

        if (CollectionUtils.isEmpty(systemStaticDatasourceIds)) {
            return super.getAllMatchingPermissions(userId, nonSystemStaticDatasourceIds, resourceAction);
        }
        return super.getAllMatchingPermissions(userId, nonSystemStaticDatasourceIds, resourceAction)
                .map(allMatchingPermissions -> {
                    Map<String, List<ResourcePermission>> result = Maps.newHashMap();
                    Map<String, List<ResourcePermission>> systemStaticDatasourcePermissions = systemStaticDatasourceIds.stream()
                            .collect(Collectors.toMap(Function.identity(), id -> getSystemStaticDatasourcePermission(userId, id)));
                    result.putAll(systemStaticDatasourcePermissions);
                    result.putAll(allMatchingPermissions);
                    return result;
                });
    }

    /**
     * 获取系统静态数据源的权限。
     *
     * @param userId 用户 ID
     * @param datasourceId 数据源 ID
     * @return 包含单个 ResourcePermission 对象的列表
     */
    private List<ResourcePermission> getSystemStaticDatasourcePermission(String userId, String datasourceId) {
        return Collections.singletonList(ResourcePermission.builder()
                .resourceId(datasourceId)
                .resourceType(ResourceType.DATASOURCE)
                .resourceHolder(USER)
                .resourceHolderId(userId)
                .resourceRole(SYSTEM_STATIC_DATASOURCE_USER_ROLE)
                .build());
    }
}
