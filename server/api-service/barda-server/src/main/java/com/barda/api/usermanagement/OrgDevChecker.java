package com.barda.api.usermanagement;

import static com.barda.sdk.util.ExceptionUtils.ofError;

import javax.annotation.Nonnull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.barda.api.home.SessionUserService;
import com.barda.domain.group.service.GroupMemberService;
import com.barda.domain.group.service.GroupService;
import com.barda.sdk.exception.BizError;

import reactor.core.publisher.Mono;

/**
 * 组织开发者检查器。
 *
 */
@Component
public class OrgDevChecker {

    /**
     * 用于获取当前会话用户的服务。
     */
    @Autowired
    private SessionUserService sessionUserService;

    /**
     * 组相关的服务。
     */
    @Autowired
    private GroupService groupService;

    /**
     * 组成员相关的服务。
     */
    @Autowired
    private GroupMemberService groupMemberService;

    /**
     * 检查当前用户是否是组织管理员或开发者。
     *
     * @return 如果是，返回空的Mono，否则返回一个包含{@link BizError#NEED_DEV_TO_CREATE_RESOURCE}的Mono
     */
    public Mono<Void> checkCurrentOrgDev() {
        return isCurrentOrgDev()
                .flatMap(result -> {
                    if (result) {
                        return Mono.empty();
                    }
                    return ofError(BizError.NEED_DEV_TO_CREATE_RESOURCE, "NEED_DEV_TO_CREATE_RESOURCE");
                });
    }

    /**
     * 检查当前用户是否是组织管理员或开发者。
     *
     * @return 如果是，返回true的Mono，否则返回false的Mono
     */
    public Mono<Boolean> isCurrentOrgDev() {
        return sessionUserService.getVisitorOrgMemberCache()
                .flatMap(orgMember -> {
                    if (orgMember.isAdmin()) {
                        return Mono.just(true);
                    }
                    return inDevGroup(orgMember.getOrgId(), orgMember.getUserId());
                });
    }

    /**
     * 检查用户是否在开发者组中。
     *
     * @param orgId  组织ID
     * @param userId 用户ID
     * @return 如果在，返回true的Mono，否则返回false的Mono
     */
    @Nonnull
    private Mono<Boolean> inDevGroup(String orgId, String userId) {
        return groupService.getDevGroup(orgId)
                .flatMap(group -> groupMemberService.isMember(group, userId))
                .defaultIfEmpty(false);
    }
}
