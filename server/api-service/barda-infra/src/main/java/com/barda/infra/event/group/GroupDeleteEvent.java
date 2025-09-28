package com.barda.infra.event.group;

import com.barda.infra.event.EventType;

import lombok.experimental.SuperBuilder;

/**
 * GroupDeleteEvent 类，表示组删除事件。
 *
 * 该类继承自 BaseGroupEvent 类，并使用 Lombok 的 @SuperBuilder 注解来生成超级构造函数。
 *
 */
@SuperBuilder
public class GroupDeleteEvent extends BaseGroupEvent {

    /**
     * 重写 getEventType() 方法来返回事件类型。
     *
     * 该方法返回 EventType.GROUP_DELETE，表示组删除事件。
     */
    @Override
    public EventType getEventType() {
        return EventType.GROUP_DELETE;
    }
}
