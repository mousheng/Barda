package com.barda.domain.permission.model;

/**
 * 表示用户在特定资源状态下的权限的类。
 * 该类提供方法来检查用户是否有权执行特定操作，并处理失败的情况。
 */
public class UserPermissionOnResourceStatus {
    private final ResourcePermission permission;

    private final FailReason failReason;

    private UserPermissionOnResourceStatus(ResourcePermission permission, FailReason failReason) {
        this.failReason = failReason;
        this.permission = permission;
    }

    /**
     * 创建一个表示成功的 UserPermissionOnResourceStatus 实例。
     *
     * @param permission 资源权限
     * @return UserPermissionOnResourceStatus 实例
     */
    public static UserPermissionOnResourceStatus success(ResourcePermission permission) {
        return new UserPermissionOnResourceStatus(permission, null);
    }

    /**
     * 创建一个表示失败的 UserPermissionOnResourceStatus 实例。
     *
     * @param failReason 失败原因
     * @return UserPermissionOnResourceStatus 实例
     */
    public static UserPermissionOnResourceStatus fail(FailReason failReason) {
        return new UserPermissionOnResourceStatus(null, failReason);
    }

    /**
     * 创建一个表示用户不在组织中的 UserPermissionOnResourceStatus 实例。
     *
     * @return UserPermissionOnResourceStatus 实例
     */
    public static UserPermissionOnResourceStatus notInOrg() {
        return new UserPermissionOnResourceStatus(null, FailReason.NOT_IN_ORG);
    }

    /**
     * 创建一个表示匿名用户的 UserPermissionOnResourceStatus 实例。
     *
     * @return UserPermissionOnResourceStatus 实例
     */
    public static UserPermissionOnResourceStatus anonymousUser() {
        return new UserPermissionOnResourceStatus(null, FailReason.ANONYMOUS_USER);
    }

    /**
     * 创建一个表示用户没有足够的权限的 UserPermissionOnResourceStatus 实例。
     *
     * @return UserPermissionOnResourceStatus 实例
     */
    public static UserPermissionOnResourceStatus notEnoughPermission() {
        return new UserPermissionOnResourceStatus(null, FailReason.NOT_ENOUGH_PERMISSION);
    }

    /**
     * 检查用户是否有权限。
     *
     * @return true 如果用户有权限，否则返回 false
     */
    public boolean hasPermission() {
        return permission != null;
    }

    /**
     * 获取用户的资源权限。
     *
     * @return 资源权限
     */
    public ResourcePermission getPermission() {
        return permission;
    }

    /**
     * 检查用户是否因为不在组织中而失败。
     *
     * @return true 如果用户因为不在组织中而失败，否则返回 false
     */
    public boolean failByNotInOrg() {
        return failReason == FailReason.NOT_IN_ORG;
    }

    /**
     * 检查用户是否因为没有足够的权限而失败。
     *
     * @return true 如果用户因为没有足够的权限而失败，否则返回 false
     */
    public boolean failByNotEnoughPermission() {
        return failReason == FailReason.NOT_ENOUGH_PERMISSION;
    }

    /**
     * 检查用户是否因为是匿名用户而失败。
     *
     * @return true 如果用户因为是匿名用户而失败，否则返回 false
     */
    public boolean failByAnonymousUser() {
        return failReason == FailReason.ANONYMOUS_USER;
    }

    /**
 * 枚举类，表示用户权限失败的原因。
 * 它包含三种情况：ANONYMOUS_USER、NOT_IN_ORG 和 NOT_ENOUGH_PERMISSION。
 */
enum FailReason {
    /**
     * 表示用户是匿名用户的情况。
     */
    ANONYMOUS_USER,

    /**
     * 表示用户不在组织中的情况。
     */
    NOT_IN_ORG,

    /**
     * 表示用户没有足够的权限执行操作的情况。
     */
    NOT_ENOUGH_PERMISSION
}
}