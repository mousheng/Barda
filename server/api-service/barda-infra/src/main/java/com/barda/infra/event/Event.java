package com.barda.infra.event;

/**
 * Event 接口，表示事件。
 *
 * 该接口定义了事件的通用行为，包括获取事件类型。
 *
 */
public interface Event {

    /**
     * 获取事件类型。
     *
     * 该方法返回一个 EventType 枚举值，表示事件的类型。
     */
    EventType getEventType();
}
