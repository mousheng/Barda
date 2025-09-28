package com.barda.api.home;

import static com.barda.sdk.constants.GlobalContext.CURRENT_ORG_MEMBER;
import static com.barda.sdk.exception.BizError.UNABLE_TO_FIND_VALID_ORG;
import static com.barda.sdk.util.ExceptionUtils.deferredError;
import static com.barda.sdk.util.JsonUtils.fromJsonQuietly;

import java.time.Duration;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;

import com.barda.api.usermanagement.UserApiService;
import com.barda.domain.organization.model.OrgMember;
import com.barda.domain.organization.service.OrgMemberService;
import com.barda.domain.user.model.User;
import com.barda.domain.user.model.UserState;
import com.barda.domain.user.service.UserService;
import com.barda.sdk.config.CommonConfig;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 用于管理会话用户的服务实现类。
 *
 */
@Slf4j
@Service
public class SessionUserServiceImpl implements SessionUserService {

    /**
     * 通用配置。
     */
    @Autowired
    private CommonConfig commonConfig;

    /**
     * 用户服务。
     */
    @Autowired
    private UserService userService;

    /**
     * 组织成员服务。
     */
    @Autowired
    private OrgMemberService orgMemberService;

    /**
     * 用户API服务。
     */
    @Autowired
    private UserApiService userApiService;

    /**
     * 响应式Redis模板。
     */
    @Autowired
    private ReactiveRedisTemplate<String, String> reactiveTemplate;

    /**
     * 该类提供获取访问者ID的功能。
     */
    @SuppressWarnings("ReactiveStreamsNullableInLambdaInTransform")
    @Override
    public Mono<String> getVisitorId() {
        return getVisitor()
                .map(User::getId);
    }

    /**
     * 获取访客信息。
     * @see com.barda.api.framework.filter.GlobalContextFilter
     * @return 访客信息的Mono。
     */
    @Override
    public Mono<User> getVisitor() {
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> (User) securityContext.getAuthentication().getPrincipal());
    }

    /**
     * 获取访客的组织成员信息（从缓存中获取）。
     * @see com.barda.api.framework.filter.GlobalContextFilter
     * @return 访客的组织成员信息的Mono。
     */
    @SuppressWarnings("unchecked")
    @Override
    public Mono<OrgMember> getVisitorOrgMemberCache() {
        return Mono.deferContextual(contextView -> (Mono<OrgMember>) contextView.get(CURRENT_ORG_MEMBER))
                .delayUntil(orgMember -> {
                    if (orgMember == OrgMember.NOT_EXIST) {
                        return deferredError(UNABLE_TO_FIND_VALID_ORG, "UNABLE_TO_FIND_VALID_ORG");
                    }
                    return Mono.empty();
                })
                .switchIfEmpty(deferredError(UNABLE_TO_FIND_VALID_ORG, "UNABLE_TO_FIND_VALID_ORG"));
    }

    /**
     * 获取访问者的组织成员信息。
     *
     * @return 包含访问者组织成员信息的Mono对象。
     */
    @Override
    public Mono<OrgMember> getVisitorOrgMember() {
        return getVisitorId()
                .flatMap(userId -> orgMemberService.getCurrentOrgMember(userId))
                .switchIfEmpty(deferredError(UNABLE_TO_FIND_VALID_ORG, "UNABLE_TO_FIND_VALID_ORG"));
    }

    /**
     * 判断当前访问者是否为匿名用户。
     *
     * @return 包含布尔值的Mono对象，指示访问者是否为匿名用户。
     */
    @Override
    public Mono<Boolean> isAnonymousUser() {
        return getVisitor()
                .map(User::isAnonymous);
    }

    /**
     * 保存用户会话。
     *
     * @param token 令牌。
     * @param user 用户信息。
     * @param source 来源。
     *
     * @return 空的Mono。
     */
    @Override
    public Mono<Void> saveUserSession(String token, User user, String source) {
        ReactiveValueOperations<String, String> ops = reactiveTemplate.opsForValue();
        return ops.set(token, Objects.requireNonNull(user.getId()), getTokenExpireTime())
                .then(userApiService.removeInvalidTokens(user.getId()))
                .then(userApiService.saveToken(user.getId(), source, token));
    }

    /**
     * 用于延长令牌有效期的公共方法。
     *
     * @param token 令牌
     * @return       空的Mono，表示操作完成
     */
    @Override
    public Mono<Void> extendValidity(String token) {
        // 若令牌为空，返回空的Mono
        if (StringUtils.isBlank(token)) {
            return Mono.empty();
        }
        // 使用RedisTemplate延长令牌的有效期
        return reactiveTemplate.expire(token, getTokenExpireTime())
                .then();
    }

    /**
     * 获取令牌的过期时间。
     *
     * @return 令牌的过期时间。
     */
    private Duration getTokenExpireTime() {
        long maxAgeInSeconds = commonConfig.getCookie().getMaxAgeInSeconds();
        if (maxAgeInSeconds >= 0) {
            return Duration.ofSeconds(maxAgeInSeconds).plus(Duration.ofDays(1));
        }
        return Duration.ofDays(7);
    }

    /**
     * 移除用户会话。
     *
     * @param token 用户会话的令牌。
     * @return 表示完成移除操作的Mono对象。
     */
    @Override
    public Mono<Void> removeUserSession(String token) {
        if (StringUtils.isBlank(token)) {
            return Mono.empty();
        }
        ReactiveValueOperations<String, String> ops = reactiveTemplate.opsForValue();
        return ops.get(token)
                .delayUntil(__ -> ops.delete(token))
                .flatMap(userId -> userApiService.removeToken(userId, token));
    }

    /**
     * 从Cookie中解析会话用户。
     *
     * @param token 用户会话的令牌。
     * @return 包含会话用户的Mono对象。
     */
    @Override
    public Mono<User> resolveSessionUserFromCookie(String token) {
        if (StringUtils.isBlank(token)) {
            return Mono.empty();
        }
        return reactiveTemplate.opsForValue().get(token)
                .flatMap(value -> {
                    User user = fromJsonQuietly(value, User.class);
                    if (user == null) {
                        return userService.findById(value);
                    }
                    // 一些兼容的代码
                    return userService.findById(user.getId());
                })
                .filter(user -> user.getState() != UserState.DELETED);
    }

    /**
     * 检查令牌是否存在。
     *
     * @param token 要检查的令牌。
     * @return 包含布尔值的Mono对象，指示令牌是否存在。
     */
    @Override
    public Mono<Boolean> tokenExist(String token) {
        return reactiveTemplate.hasKey(token);
    }
}

