package com.barda.infra.event;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * 一个表示图书馆查询事件的类。
 * 该类扩展了 {@link AbstractEvent} 并提供特定于图书馆查询的字段。
 */
@Getter
@SuperBuilder
public class LibraryQueryEvent extends AbstractEvent {

    /**
     * 事件的唯一标识符。
     */
    private String id;

    /**
     * 事件的名称。
     */
    private String name;

    /**
     * 事件的类型。
     */
    private EventType eventType;

    /**
     * 获取事件的类型。
     *
     * @return 事件的类型
     */
    @Override
    public EventType getEventType() {
        return eventType;
    }
}
