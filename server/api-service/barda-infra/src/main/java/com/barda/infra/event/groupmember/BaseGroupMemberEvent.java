package com.barda.infra.event.groupmember;

import com.barda.infra.event.AbstractEvent;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * BaseGroupMemberEvent 类，表示基础组成员事件。
 *
 * 该类是 AbstractEvent 类的抽象子类，并使用 Lombok 的 @Getter 和 @SuperBuilder 注解来生成 getter 方法和超级构造函数。
 *
 */
@Getter
@SuperBuilder
public abstract class BaseGroupMemberEvent extends AbstractEvent {

    /**
     * 用于表示组 ID 的私有成员变量。
     *
     * 该变量用于存储组的唯一标识符。
     */
    private final String groupId;

    /**
     * 用于表示组名称的私有成员变量。
     *
     * 该变量用于存储组的名称。
     */
    private final String groupName;

    /**
     * 用于表示成员 ID 的私有成员变量。
     *
     * 该变量用于存储组成员的唯一标识符。
     */
    private final String memberId;

    /**
     * 用于表示成员名称的私有成员变量。
     *
     * 该变量用于存储组成员的名称。
     */
    private final String memberName;

    /**
     * 用于表示成员角色的私有成员变量。
     *
     * 该变量用于存储组成员的角色。
     */
    private final String memberRole;
}
