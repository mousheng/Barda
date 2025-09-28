package com.barda.infra.event.datasource;

import com.barda.infra.event.AbstractEvent;
import com.barda.infra.event.EventType;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * DatasourceEvent 类，表示数据源事件。
 *
 * 该类继承自 AbstractEvent 类，并使用 Lombok 的 @Getter 和 @SuperBuilder 注解来生成 getter 方法和超级构造函数。
 *
 */
@Getter
@SuperBuilder
public class DatasourceEvent extends AbstractEvent {

    /**
     * 用于表示数据源 ID 的私有成员变量。
     *
     * 该变量用于存储数据源的唯一标识符。
     */
    private final String datasourceId;

    /**
     * 用于表示数据源名称的私有成员变量。
     *
     * 该变量用于存储数据源的名称。
     */
    private final String name;

    /**
     * 用于表示数据源类型的私有成员变量。
     *
     * 该变量用于存储数据源的类型。
     */
    private final String type;

    /**
     * 用于表示事件类型的私有成员变量。
     *
     * 该变量用于存储事件的类型。
     */
    private final EventType eventType;
}
