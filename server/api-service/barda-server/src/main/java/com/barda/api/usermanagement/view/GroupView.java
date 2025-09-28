package com.barda.api.usermanagement.view;

import java.util.Locale;

import com.barda.domain.group.model.Group;
import com.barda.sdk.util.LocaleUtils;

import lombok.Builder;
import lombok.Getter;
import reactor.core.publisher.Mono;

/**
 * 群组视图类。
 * 该类使用 Lombok 的 @Getter 和 @Builder 注解来生成 getter 方法和构建器。
 */
@Getter
@Builder
public class GroupView {

    /**
     * 群组 ID。
     */
    private String groupId;

    /**
     * 群组名称。
     */
    private String groupName;

    /**
     * 指示是否为所有用户群组。
     */
    private boolean allUsersGroup;

    /**
     * 指示是否为开发者群组。
     */
    private boolean isDevGroup;

    /**
     * 访问者的角色。
     */
    private String visitorRole;

    /**
     * 创建时间。
     */
    private long createTime;

    /**
     * 动态规则。
     */
    private String dynamicRule;

    /**
     * 指示是否为同步群组。
     */
    private boolean isSyncGroup;

    /**
     * 指示是否为同步删除。
     */
    private boolean isSyncDelete;

    /**
     * 从群组和成员角色创建群组视图。
     *
     * @param group 群组
     * @param memberRole 成员角色
     * @return 群组视图的 Mono 对象
     */
    public static Mono<GroupView> from(Group group, String memberRole) {
        return Mono.deferContextual(contextView -> {
            Locale locale = LocaleUtils.getLocale(contextView);
            GroupView groupView = GroupView.builder()
                    .groupId(group.getId())
                    .groupName(group.getName(locale))
                    .allUsersGroup(group.isAllUsersGroup())
                    .isDevGroup(group.isDevGroup())
                    .createTime(group.getCreateTime())
                    .visitorRole(memberRole)
                    .dynamicRule(group.getDynamicRule())
                    .isSyncGroup(group.isSyncGroup())
                    .isSyncDelete(group.isSyncDeleted())
                    .build();
            return Mono.just(groupView);
        });
    }
}
