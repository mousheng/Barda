package com.barda.api.usermanagement.view;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

/**
 * 群组成员聚合视图类。
 * 该类使用 Lombok 的 @Getter 和 @Builder 注解来生成 getter 方法和构建器。
 */
@Getter
@Builder
public class GroupMemberAggregateView {

    /**
     * 访问者的角色。
     */
    private String visitorRole;

    /**
     * 群组成员列表。
     */
    private List<GroupMemberView> members;
}
