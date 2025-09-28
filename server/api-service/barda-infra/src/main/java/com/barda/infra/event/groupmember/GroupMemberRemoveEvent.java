package com.barda.infra.event.groupmember;

import com.barda.infra.event.EventType;

import lombok.experimental.SuperBuilder;

/**
 * GroupMemberRemoveEvent 类，表示组成员移除事件。
 *
 * 该类继承自 BaseGroupMemberEvent 类，并使用 Lombok 的 @SuperBuilder 注解来生成超级构造函数。
 *
 */
@SuperBuilder
public class GroupMemberRemoveEvent extends BaseGroupMemberEvent {

    /**
     * 重写 getEventType() 方法来返回事件类型。
     *
     * 该方法返回 EventType.GROUP_MEMBER_REMOVE，表示组成员移除事件。
     */
    @Override
    public EventType getEventType() {
        return EventType.GROUP_MEMBER_REMOVE;
    }
}
