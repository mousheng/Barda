package com.barda.api.application.view;

import com.barda.api.permission.view.CommonPermissionView;

import lombok.experimental.SuperBuilder;

/**
 * 应用权限视图类，继承自通用权限视图类，用于在应用列表中返回应用的权限信息。
 * 该类使用了 Lombok 的 @SuperBuilder 注解来生成构造器，可以方便地创建该类的实例。
 */
@SuperBuilder
public class ApplicationPermissionView extends CommonPermissionView {

    /**
     * 应用是否对所有人公开
     */
    private boolean publicToAll;

    /**
     * 获取应用是否对所有人公开的状态
     * @return true表示应用对所有人公开，false表示应用不对所有人公开
     */
    public boolean isPublicToAll() {
        return publicToAll;
    }
}
