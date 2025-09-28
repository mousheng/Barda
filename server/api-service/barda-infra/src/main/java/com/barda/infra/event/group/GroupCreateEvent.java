package com.barda.infra.event.group;

import com.barda.infra.event.EventType;

import lombok.experimental.SuperBuilder;

/**
 * GroupCreateEvent 类，表示组创建事件。
 *
 * 该类继承自 BaseGroupEvent 类，并使用 Lombok 的 @SuperBuilder 注解来生成超级构造函数。
 *
 */
@SuperBuilder
public class GroupCreateEvent extends BaseGroupEvent {

    /**
     * 重写 getEventType() 方法来返回事件类型。
     *
     * 该方法返回 EventType.GROUP_CREATE，表示组创建事件。
     */
    @Override
    public EventType getEventType() {
        return EventType.GROUP_CREATE;
    }
}
