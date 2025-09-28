package com.barda.domain.permission.service;

import static com.google.common.collect.Sets.newHashSet;
import static com.barda.domain.permission.model.ResourceHolder.USER;
import static com.barda.sdk.constants.Authentication.ANONYMOUS_USER_ID;
import static com.barda.sdk.util.StreamUtils.collectMap;
import static java.util.Collections.emptyMap;
import static java.util.function.Function.identity;
import static org.apache.commons.collections4.SetUtils.union;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.barda.domain.application.model.Application;
import com.barda.domain.application.service.ApplicationService;
import com.barda.domain.permission.model.ResourceAction;
import com.barda.domain.permission.model.ResourcePermission;
import com.barda.domain.permission.model.ResourceRole;
import com.barda.domain.permission.model.ResourceType;
import com.barda.domain.solutions.TemplateSolution;

import reactor.core.publisher.Mono;

/**
 * 应用程序权限处理器的实现类，继承自 ResourcePermissionHandler。
 * 它使用懒加载模式创建，并使用 Spring 组件注解。
 */
@Lazy
@Component
class ApplicationPermissionHandler extends ResourcePermissionHandler {

    private static final ResourceRole ANONYMOUS_USER_ROLE = ResourceRole.VIEWER;

    /**
     * 应用程序服务的注入点。
     */
    @Autowired
    private ApplicationService applicationService;

    /**
     * 模板解决方案的注入点。
     */
    @Autowired
    private TemplateSolution templateSolution;

    /**
     * 获取匿名用户在特定资源上的权限。
     *
     * @param resourceIds 资源 ID 集合
     * @param resourceAction 资源操作
     * @return 包含资源权限的 Map，键为资源 ID，值为权限列表
     */
    @Override
    protected Mono<Map<String, List<ResourcePermission>>> getAnonymousUserPermissions(Collection<String> resourceIds,
            ResourceAction resourceAction) {
        if (!ANONYMOUS_USER_ROLE.canDo(resourceAction)) {
            return Mono.just(emptyMap());
        }

        Set<String> applicationIds = newHashSet(resourceIds);
        return Mono.zip(applicationService.getPublicApplicationIds(applicationIds),
                        templateSolution.getTemplateApplicationIds(applicationIds))
                .map(tuple -> {
                    Set<String> publicAppIds = tuple.getT1();
                    Set<String> templateAppIds = tuple.getT2();
                    return collectMap(union(publicAppIds, templateAppIds), identity(), this::getAnonymousUserPermission);
                });
    }

    /**
     * 获取匿名用户在特定应用程序上的权限。
     *
     * @param applicationId 应用程序 ID
     * @return 包含单个 ResourcePermission 对象的列表
     */
    private List<ResourcePermission> getAnonymousUserPermission(String applicationId) {
        return Collections.singletonList(ResourcePermission.builder()
                .resourceId(applicationId)
                .resourceType(ResourceType.APPLICATION)
                .resourceHolder(USER)
                .resourceHolderId(ANONYMOUS_USER_ID)
                .resourceRole(ANONYMOUS_USER_ROLE)
                .build());
    }

    /**
     * 获取特定资源的组织 ID。
     *
     * @param resourceId 资源 ID
     * @return 组织 ID 的 Mono
     */
    @Override
    protected Mono<String> getOrgId(String resourceId) {
        return applicationService.findByIdWithoutDsl(resourceId)
                .map(Application::getOrganizationId);
    }
}
