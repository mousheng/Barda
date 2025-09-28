package com.barda.infra.event;

import java.util.Locale;

import com.barda.sdk.util.LocaleUtils;

/**
 * 事件类型枚举，用于表示不同的事件类型。
 */
public enum EventType {

    // 用户登录事件
    USER_LOGIN("EVENT_TYPE_USER_LOGIN"),
    // 用户登出事件
    USER_LOGOUT("EVENT_TYPE_USER_LOGOUT"),

    // 查看应用事件
    VIEW("EVENT_TYPE_VIEW"),
    // 创建应用事件
    APPLICATION_CREATE("EVENT_TYPE_APPLICATION_CREATE"),
    // 删除应用事件
    APPLICATION_DELETE("EVENT_TYPE_APPLICATION_DELETE"),
    // 更新应用事件
    APPLICATION_UPDATE("EVENT_TYPE_APPLICATION_UPDATE"),
    // 移动应用事件
    APPLICATION_MOVE("EVENT_TYPE_APPLICATION_MOVE"),
    // 回收站应用事件
    APPLICATION_RECYCLED("EVENT_TYPE_APPLICATION_RECYCLED"),
    // 恢复应用事件
    APPLICATION_RESTORE("EVENT_TYPE_APPLICATION_RESTORE"),

    // 创建文件夹事件
    FOLDER_CREATE("EVENT_TYPE_FOLDER_CREATE"),
    // 删除文件夹事件
    FOLDER_DELETE("EVENT_TYPE_FOLDER_DELETE"),
    // 更新文件夹事件
    FOLDER_UPDATE("EVENT_TYPE_FOLDER_UPDATE"),

    // 查询执行事件
    QUERY_EXECUTION("EVENT_TYPE_QUERY_EXECUTION"),
    // 创建用户组事件
    GROUP_CREATE("EVENT_TYPE_GROUP_CREATE"),
    // 更新用户组事件
    GROUP_UPDATE("EVENT_TYPE_GROUP_UPDATE"),
    // 删除用户组事件
    GROUP_DELETE("EVENT_TYPE_GROUP_DELETE"),
    // 添加组成员事件
    GROUP_MEMBER_ADD("EVENT_TYPE_GROUP_MEMBER_ADD"),
    // 更新组成员角色事件
    GROUP_MEMBER_ROLE_UPDATE("EVENT_TYPE_GROUP_MEMBER_ROLE_UPDATE"),
    // 成员离开事件
    GROUP_MEMBER_LEAVE("EVENT_TYPE_GROUP_MEMBER_LEAVE"),
    // 移除组成员事件
    GROUP_MEMBER_REMOVE("EVENT_TYPE_GROUP_MEMBER_REMOVE"),
    // 服务器启动事件
    SERVER_START_UP("EVENT_TYPE_SERVER_START_UP"),

    // 创建数据源事件
    DATA_SOURCE_CREATE("DATA_SOURCE_CREATE"),
    // 更新数据源事件
    DATA_SOURCE_UPDATE("DATA_SOURCE_UPDATE"),
    // 删除数据源事件
    DATA_SOURCE_DELETE("DATA_SOURCE_DELETE"),
    // 授权数据源事件
    DATA_SOURCE_PERMISSION_GRANT("DATA_SOURCE_PERMISSION_GRANT"),
    // 更新数据源权限事件
    DATA_SOURCE_PERMISSION_UPDATE("DATA_SOURCE_PERMISSION_UPDATE"),
    // 删除数据源权限事件
    DATA_SOURCE_PERMISSION_DELETE("DATA_SOURCE_PERMISSION_DELETE"),

    // 创建库查询事件
    LIBRARY_QUERY_CREATE("LIBRARY_QUERY_CREATE"),
    // 更新库查询事件
    LIBRARY_QUERY_UPDATE("LIBRARY_QUERY_UPDATE"),
    // 删除库查询事件
    LIBRARY_QUERY_DELETE("LIBRARY_QUERY_DELETE"),
    // 发布库查询事件
    LIBRARY_QUERY_PUBLISH("LIBRARY_QUERY_PUBLISH"),
    ;

    private final String desc;

    EventType(String desc) {
        this.desc = desc;
    }

    /**
     * 根据给定的语言环境获取事件描述。
     * @param locale 语言环境
     * @return 事件描述
     */
    public String getDesc(Locale locale) {
        return LocaleUtils.getMessage(locale, this.desc);
    }
}

