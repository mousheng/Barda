package com.barda.api.home;

import com.barda.domain.organization.model.OrgMember;
import com.barda.domain.user.model.User;
import com.barda.infra.annotation.NonEmptyMono;

import reactor.core.publisher.Mono;

/**
 * 用于管理会话用户的服务接口。
 *
 */
public interface SessionUserService {

    /**
     * 获取访客信息。
     *
     * @return 访客信息的Mono。
     */
    @NonEmptyMono
    Mono<User> getVisitor();

    /**
     * 获取访客的ID。
     *
     * @return 访客ID的Mono。
     */
    @NonEmptyMono
    Mono<String> getVisitorId();

    /**
     * 获取访客的组织成员信息（从缓存中获取）。
     *
     * @return 访客的组织成员信息的Mono。
     */
    @NonEmptyMono
    Mono<OrgMember> getVisitorOrgMemberCache();

    /**
     * 获取访客的组织成员信息。
     *
     * @return 访客的组织成员信息的Mono。
     */
    Mono<OrgMember> getVisitorOrgMember();

    /**
     * 判断是否为匿名用户。
     *
     * @return true表示是匿名用户，false表示不是匿名用户。
     */
    Mono<Boolean> isAnonymousUser();

    /**
     * 保存用户会话。
     *
     * @param sessionId 会话ID。
     * @param user 用户信息。
     * @param source 来源。
     *
     * @return 空的Mono。
     */
    Mono<Void> saveUserSession(String sessionId, User user, String source);

    /**
     * 延长会话的有效期。
     *
     * @param sessionId 会话ID。
     *
     * @return 空的Mono。
     */
    Mono<Void> extendValidity(String sessionId);

    /**
     * 移除用户会话。
     *
     * @param sessionId 会话ID。
     *
     * @return 空的Mono。
     */
    Mono<Void> removeUserSession(String sessionId);

    /**
     * 从Cookie中解析会话用户。
     *
     * @param token 令牌。
     *
     * @return 会话用户的Mono。
     */
    Mono<User> resolveSessionUserFromCookie(String token);

    /**
     * 判断令牌是否存在。
     *
     * @param token 令牌。
     *
     * @return true表示令牌存在，false表示令牌不存在。
     */
    Mono<Boolean> tokenExist(String token);
}
