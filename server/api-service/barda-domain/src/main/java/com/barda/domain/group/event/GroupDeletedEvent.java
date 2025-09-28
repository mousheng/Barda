package com.barda.domain.group.event;

import lombok.Getter;
import lombok.Setter;

/**
 * 该类表示已删除的组的事件。
 */
@Getter
@Setter
public class GroupDeletedEvent {

    /**
     * 已删除组的 ID。
     */
    private String groupId;
}
