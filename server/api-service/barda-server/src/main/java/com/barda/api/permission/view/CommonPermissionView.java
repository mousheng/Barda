package com.barda.api.permission.view;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * 通用权限视图类。
 * 该类用于封装组织的权限信息，包括组权限和用户权限。
 */
@Getter
@SuperBuilder
public class CommonPermissionView {

    /**
     * 所属组织名称。
     */
    private String orgName;

    /**
     * 组权限列表。
     */
    private List<PermissionItemView> groupPermissions;

    /**
     * 用户权限列表。
     */
    private List<PermissionItemView> userPermissions;

    /**
     * 创建者ID。
     */
    private String creatorId;

    /**
     * 获取所有权限列表。
     * 该方法将组权限和用户权限合并成一个列表返回。
     *
     * @return 所有权限列表
     */
    public List<PermissionItemView> getPermissions() {
        ArrayList<PermissionItemView> permissionPairs = new ArrayList<>();
        permissionPairs.addAll(groupPermissions);
        permissionPairs.addAll(userPermissions);
        return permissionPairs;
    }
}
