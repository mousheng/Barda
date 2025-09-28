package com.barda.infra.event.group;

import com.barda.infra.event.EventType;

import lombok.experimental.SuperBuilder;

/**
 * GroupUpdateEvent 类，表示组更新事件。
 *
 * 该类继承自 BaseGroupEvent 类，并使用 Lombok 的 @SuperBuilder 注解来生成超级构造函数。
 *
 */
@SuperBuilder
public class GroupUpdateEvent extends BaseGroupEvent {

    /**
     * 重写 getEventType() 方法来返回事件类型。
     *
     * 该方法返回 EventType.GROUP_UPDATE，表示组更新事件。
     */
    @Override
    public EventType getEventType() {
        return EventType.GROUP_UPDATE;
    }
}
