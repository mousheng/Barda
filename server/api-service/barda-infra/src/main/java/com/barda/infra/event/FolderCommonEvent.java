package com.barda.infra.event;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * 一个表示与文件夹相关的通用事件的类。
 * 该类扩展了 {@link AbstractEvent} 并提供特定于文件夹的字段。
 */
@Getter
@SuperBuilder
public class FolderCommonEvent extends AbstractEvent {

    /**
     * 事件的唯一标识符。
     */
    private final String id;

    /**
     * 事件的名称。
     */
    private final String name;

    /**
     * 事件的类型。
     */
    private final EventType type;

    /**
     * 获取事件的类型。
     *
     * @return 事件的类型
     */
    @Override
    public EventType getEventType() {
        return type;
    }
}
