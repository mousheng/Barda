package com.barda.infra.event;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * AbstractEvent 类，表示抽象事件。
 *
 * 该类实现了 Event 接口，并使用 Lombok 的 @Getter 和 @SuperBuilder 注解来生成 getter 方法和超级构造函数。
 *
 */
@Getter
@SuperBuilder
public abstract class AbstractEvent implements Event {

    /**
     * 用于表示组织 ID 的私有成员变量。
     *
     * 该变量用于存储事件所属的组织的唯一标识符。
     */
    protected final String orgId;

    /**
     * 用于表示用户 ID 的私有成员变量。
     *
     * 该变量用于存储事件的创建者的唯一标识符。
     */
    protected final String userId;
}
