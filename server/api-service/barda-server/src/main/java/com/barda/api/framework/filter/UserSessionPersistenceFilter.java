package com.barda.api.framework.filter;

import static com.barda.api.authentication.util.AuthenticationUtils.toAuthentication;
import static org.springframework.security.core.context.ReactiveSecurityContextHolder.withAuthentication;

import javax.annotation.Nonnull;

import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.barda.api.home.SessionUserService;
import com.barda.sdk.util.CookieHelper;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 一个WebFilter实现类，用于对用户的会话进行持久化。
 * 该类使用了Slf4j来进行日志记录。
 * 该类使用了SessionUserService和CookieHelper来获取会话信息和Cookie。
 */
@Slf4j
public class UserSessionPersistenceFilter implements WebFilter {

    /**
     * SessionUserService的实例，用于获取会话信息
     */
    private final SessionUserService service;

    /**
     * CookieHelper的实例，用于获取和操作Cookie
     */
    private final CookieHelper cookieHelper;

    /**
     * 构造函数，用于初始化UserSessionPersistenceFilter
     *
     * @param service     SessionUserService的实例
     * @param cookieHelper CookieHelper的实例
     */
    public UserSessionPersistenceFilter(SessionUserService service, CookieHelper cookieHelper) {
        this.service = service;
        this.cookieHelper = cookieHelper;
    }

    /**
     * 重写filter()方法，用于对HTTP请求进行会话持久化
     *
     * @param exchange  ServerWebExchange，表示一个HTTP请求和响应
     * @param chain     WebFilterChain，表示一个WebFilter的链
     * @return  一个Mono<Void>，表示处理完成
     */
    @Nonnull
    @Override
    public Mono<Void> filter(@Nonnull ServerWebExchange exchange, WebFilterChain chain) {
        // 获取 Cookie 中的令牌
        String cookieToken = cookieHelper.getCookieToken(exchange);
        // 从 Cookie 解析出会话用户
        return service.resolveSessionUserFromCookie(cookieToken)
                // 如果解析出来的会话用户为空，则直接通过 WebFilterChain
                .switchIfEmpty(chain.filter(exchange).then(Mono.empty()))
                // 如果解析出来的会话用户不为空，则执行以下操作
                .flatMap(user ->
                    // 先对 HTTP 请求进行过滤，然后在上下文中写入认证信息
                    chain.filter(exchange).contextWrite(withAuthentication(toAuthentication(user)))
                        // 最后，延长 Cookie 有效期
                        .then(service.extendValidity(cookieToken))
                );
    }
}
