package com.barda.infra.event.group;

import com.barda.infra.event.AbstractEvent;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * BaseGroupEvent 类，表示基础组事件。
 *
 * 该类是 AbstractEvent 类的抽象子类，并使用 Lombok 的 @Getter 和 @SuperBuilder 注解来生成 getter 方法和超级构造函数。
 *
 */
@Getter
@SuperBuilder
public abstract class BaseGroupEvent extends AbstractEvent {

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
}
