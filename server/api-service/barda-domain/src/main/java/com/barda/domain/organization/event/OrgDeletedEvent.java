package com.barda.domain.organization.event;

import lombok.Getter;
import lombok.Setter;

/**
 * 组织删除事件类。
 * 该类包含了组织删除时所需的相关信息。
 */
@Getter
@Setter
public class OrgDeletedEvent {

    /**
     * 组织ID。
     */
    private String orgId;

}