package com.barda.infra.event.datasource;

import java.util.Collection;

import com.barda.infra.event.AbstractEvent;
import com.barda.infra.event.EventType;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * DatasourcePermissionEvent 类，表示数据源权限事件。
 *
 * 该类继承自 AbstractEvent 类，并使用 Lombok 的 @Getter 和 @SuperBuilder 注解来生成 getter 方法和超级构造函数。
 *
 */
@Getter
@SuperBuilder
public class DatasourcePermissionEvent extends AbstractEvent {

    /**
     * 用于表示数据源 ID 的私有成员变量。
     *
     * 该变量用于存储数据源的唯一标识符。
     */
    private final String datasourceId;

    /**
     * 用于表示数据源名称的私有成员变量。
     *
     * 该变量用于存储数据源的名称。
     */
    private final String name;

    /**
     * 用于表示数据源类型的私有成员变量。
     *
     * 该变量用于存储数据源的类型。
     */
    private final String type;

    /**
     * 用于表示用户 ID 集合的私有成员变量。
     *
     * 该集合用于存储与数据源相关的用户 ID。
     */
    private final Collection<String> userIds;

    /**
     * 用于表示组 ID 集合的私有成员变量。
     *
     * 该集合用于存储与数据源相关的组 ID。
     */
    private final Collection<String> groupIds;

    /**
     * 用于表示角色的私有成员变量。
     *
     * 该变量用于存储与数据源相关的角色。
     */
    private final String role;

    /**
     * 用于表示事件类型的私有成员变量。
     *
     * 该变量用于存储事件的类型。
     */
    private final EventType eventType;
}
