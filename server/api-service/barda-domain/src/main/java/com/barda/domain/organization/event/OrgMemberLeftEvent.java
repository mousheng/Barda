package com.barda.domain.organization.event;

import lombok.Getter;

/**
 * 组织成员离开事件类。
 * 该类包含了组织成员离开时所需的相关信息。
 */
@Getter
public class OrgMemberLeftEvent {

    /**
     * 组织ID。
     */
    private final String orgId;

    /**
     * 用户ID。
     */
    private final String userId;

    /**
     * 构造器。
     *
     * @param orgId 组织ID
     * @param userId 用户ID
     */
    public OrgMemberLeftEvent(String orgId, String userId) {
        this.orgId = orgId;
        this.userId = userId;
    }
}
