package com.barda.api.permission.view;

import lombok.Builder;
import lombok.Getter;

import com.barda.domain.permission.model.ResourceHolder;

/**
 * 权限项视图类。
 * 该类用于封装单个权限项的相关信息，如权限ID、资源类型、ID、头像、名称和角色。
 */
@Builder
@Getter
public class PermissionItemView {

    /**
     * 权限ID。
     */
    private String permissionId;

    /**
     * 资源类型。
     * 该字段使用枚举类型 {@link ResourceHolder} 进行定义。
     */
    private ResourceHolder type;

    /**
     * ID。
     * 该ID可能是用户ID、组ID等，具体取决于 {@link ResourceHolder} 的值。
     */
    private String id;

    /**
     * 头像。
     * 用于表示拥有该权限的对象的头像。
     */
    private String avatar;

    /**
     * 名称。
     * 用于表示拥有该权限的对象的名称。
     */
    private String name;

    /**
     * 角色。
     * 用于表示拥有该权限的对象的角色。
     */
    private String role;
}