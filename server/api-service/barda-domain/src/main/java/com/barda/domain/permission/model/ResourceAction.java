package com.barda.domain.permission.model;

import static com.google.common.collect.Multimaps.toMultimap;
import static java.util.Collections.emptySet;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

import java.util.Arrays;
import java.util.Set;

import javax.annotation.Nonnull;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import lombok.Getter;

/**
 * 资源操作枚举类。
 * 该枚举类定义了与资源相关的操作，并将它们与相应的角色和资源类型相关联。
 */
@Getter
public enum ResourceAction {

    /**
     * 管理应用。
     * 只有拥有者角色的用户可以执行此操作。
     */
    MANAGE_APPLICATIONS(ResourceRole.OWNER, ResourceType.APPLICATION),

    /**
     * 读取应用。
     * 具有查看者角色的用户可以执行此操作。
     */
    READ_APPLICATIONS(ResourceRole.VIEWER, ResourceType.APPLICATION),

    /**
     * 发布应用。
     * 只有编辑者角色的用户可以执行此操作。
     */
    PUBLISH_APPLICATIONS(ResourceRole.EDITOR, ResourceType.APPLICATION),

    /**
     * 导出应用。
     * 只有编辑者角色的用户可以执行此操作。
     */
    EXPORT_APPLICATIONS(ResourceRole.EDITOR, ResourceType.APPLICATION),

    /**
     * 编辑应用。
     * 只有编辑者角色的用户可以执行此操作。
     */
    EDIT_APPLICATIONS(ResourceRole.EDITOR, ResourceType.APPLICATION),

    /**
     * 设置应用为公开。
     * 只有编辑者角色的用户可以执行此操作。
     */
    SET_APPLICATIONS_PUBLIC(ResourceRole.EDITOR, ResourceType.APPLICATION),

    /**
     * 管理数据源。
     * 只有拥有者角色的用户可以执行此操作。
     */
    MANAGE_DATASOURCES(ResourceRole.OWNER, ResourceType.DATASOURCE),

    /**
     * 使用数据源。
     * 具有查看者角色的用户可以执行此操作。
     */
    USE_DATASOURCES(ResourceRole.VIEWER, ResourceType.DATASOURCE),

    ;

    private static final SetMultimap<ResourceRole, ResourceAction> ROLE_PERMISSIONS;

    static {
        ROLE_PERMISSIONS = Arrays.stream(values())
                .collect(toMultimap(ResourceAction::getRole, it -> it, HashMultimap::create));
    }

    private final ResourceRole role;
    private final ResourceType resourceType;

    ResourceAction(ResourceRole role, ResourceType resourceType) {
        this.role = role;
        this.resourceType = resourceType;
    }

    /**
     * 获取与指定角色匹配的操作。
     *
     * @param role 角色
     * @return 匹配的操作集合
     */
    @Nonnull
    static Set<ResourceAction> getMatchingPermissions(ResourceRole role) {
        return firstNonNull(ROLE_PERMISSIONS.get(role), emptySet());
    }
}
