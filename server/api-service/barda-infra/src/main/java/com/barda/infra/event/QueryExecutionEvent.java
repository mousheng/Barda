package com.barda.infra.event;

import java.util.Map;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * 一个表示查询执行事件的类。
 * 该类扩展了 {@link AbstractEvent} 并提供特定于查询执行的字段。
 */
@Getter
@SuperBuilder
public class QueryExecutionEvent extends AbstractEvent {

    /**
     * 事件的详细信息，通常包含有关查询执行的更多数据。
     */
    private final Map<String, Object> detail;

    /**
     * 获取事件的类型。
     *
     * @return 事件的类型，在本例中为 {@link EventType#QUERY_EXECUTION}
     */
    @Override
    public EventType getEventType() {
        return EventType.QUERY_EXECUTION;
    }
}
