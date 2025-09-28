package com.barda.infra.event;

import javax.annotation.Nullable;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * ApplicationCommonEvent 类，表示应用通用事件。
 *
 * 该类继承自 AbstractEvent 类，并使用 Lombok 的 @Getter 和 @SuperBuilder 注解来生成 getter 方法和超级构造函数。
 *
 */
@Getter
@SuperBuilder
public class ApplicationCommonEvent extends AbstractEvent {

    /**
     * 用于表示应用 ID 的私有成员变量。
     *
     * 该变量用于存储应用的唯一标识符。
     */
    private final String applicationId;

    /**
     * 用于表示应用名称的私有成员变量。
     *
     * 该变量用于存储应用的名称。
     */
    private final String applicationName;

    /**
     * 用于表示事件类型的私有成员变量。
     *
     * 该变量使用 EventType 枚举来表示事件的类型。
     */
    private final EventType type;

    /**
     * 用于表示文件夹 ID 的可空私有成员变量。
     *
     * 该变量用于存储应用所属文件夹的唯一标识符。可以为 null，表示应用不属于任何文件夹。
     */
    @Nullable
    private final String folderId;

    /**
     * 用于表示文件夹名称的可空私有成员变量。
     *
     * 该变量用于存储应用所属文件夹的名称。可以为 null，表示应用不属于任何文件夹。
     */
    @Nullable
    private final String folderName;

    /**
     * 重写 getEventType() 方法来返回事件类型。
     *
     * 该方法返回 type 成员变量的值，表示事件的类型。
     */
    @Override
    public EventType getEventType() {
        return type;
    }
}
