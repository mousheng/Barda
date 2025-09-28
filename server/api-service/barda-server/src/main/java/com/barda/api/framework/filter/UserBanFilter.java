package com.barda.api.framework.filter;

import static com.barda.api.framework.filter.FilterOrder.USER_BAN;
import static com.barda.sdk.constants.Authentication.isAnonymousUser;
import static com.barda.sdk.exception.BizError.USER_BANNED;
import static com.barda.sdk.util.ExceptionUtils.ofError;

import javax.annotation.Nonnull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.barda.api.home.SessionUserService;
import com.barda.domain.user.model.UserStatus;
import com.barda.domain.user.service.UserStatusService;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 一个WebFilter实现类，用于对用户的封禁状态进行检查。
 * 该类使用了Slf4j来进行日志记录，并实现了WebFilter和Ordered接口。
 * 该类在WebFilterChain中执行的顺序由getOrder()方法返回的值确定。
 * 该类使用了UserStatusService和SessionUserService来获取用户的封禁状态和会话信息。
 */
@Component
@Slf4j
public class UserBanFilter implements WebFilter, Ordered {

    /**
     * UserStatusService的实例，用于获取用户的封禁状态
     */
    @Autowired
    private UserStatusService userStatusService;

    /**
     * SessionUserService的实例，用于获取会话信息
     */
    @Autowired
    private SessionUserService sessionUserService;

    /**
     * 重写filter()方法，用于对HTTP请求进行封禁状态检查
     *
     * @param exchange  ServerWebExchange，表示一个HTTP请求和响应
     * @param chain     WebFilterChain，表示一个WebFilter的链
     * @return  一个Mono<Void>，表示处理完成
     */
    @Nonnull
    @Override
    public Mono<Void> filter(@Nonnull ServerWebExchange exchange, @Nonnull WebFilterChain chain) {
        return sessionUserService.getVisitorId()
                .flatMap(visitorId -> {
                    if (isAnonymousUser(visitorId)) {
                        return Mono.empty();
                    }

                    return userStatusService.findByUserId(visitorId)
                            .map(UserStatus::isBanned)
                            .defaultIfEmpty(false)
                            .flatMap(isBanned -> {
                                if (isBanned) {
                                    return ofError(USER_BANNED, "USER_BANNED");
                                }
                                return Mono.empty();
                            });
                })
                .then(Mono.defer(() -> chain.filter(exchange)));
    }

    /**
     * 重写getOrder()方法，用于确定该WebFilter在WebFilterChain中的执行顺序
     * 值越小，执行的优先级越高
     *
     * @return  该WebFilter在WebFilterChain中的执行顺序
     */
    @Override
    public int getOrder() {
        return USER_BAN.getOrder();
    }
}

