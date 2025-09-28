package com.barda.api.usermanagement.view;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * 组织成员列表视图类。
 * 该类使用 Lombok 的 @Getter 和 @Builder 注解来生成 getter 方法和构建器。
 */
@Getter
@Builder
public class OrgMemberListView {

    /**
     * 访问者的角色。
     */
    private String visitorRole;

    /**
     * 组织成员列表。
     */
    private List<OrgMemberView> members;

    /**
     * 组织成员视图类。
     * 该类使用 Lombok 的 @Getter 和 @SuperBuilder 注解来生成 getter 方法和超类构建器。
     */
    @Getter
    @SuperBuilder
    public static class OrgMemberView {

        /**
         * 用户 ID。
         */
        private String userId;

        /**
         * 用户名称。
         */
        private String name;

        /**
         * 用户头像 URL。
         */
        private String avatarUrl;

        /**
         * 用户的角色。
         */
        private String role;

        /**
         * 用户加入的时间。
         */
        private long joinTime;

        /**
         * 原始用户信息。
         * 该字段是一个嵌套的 Map，其中键为来源的名称，值为原始用户信息的 Map。
         */
        private Map<String, Map<String, Object>> rawUserInfos;
    }
}
