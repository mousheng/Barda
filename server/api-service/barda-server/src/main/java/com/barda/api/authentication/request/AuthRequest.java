package com.barda.api.authentication.request;

import com.barda.domain.authentication.context.AuthRequestContext;
import com.barda.domain.user.model.AuthToken;
import com.barda.domain.user.model.AuthUser;

import reactor.core.publisher.Mono;

/**
 * 定义了认证请求的接口。
 * 该接口包含了执行认证和刷新令牌的操作。
 */
public interface AuthRequest {

    /**
     * 执行认证操作。
     *
     * @param authRequestContext 认证请求上下文
     * @return 包含认证结果的 {@link Mono}
     */
    Mono<AuthUser> auth(AuthRequestContext authRequestContext);

    /**
     * 默认实现的刷新令牌操作。
     * 该方法返回一个 {@link Mono}，其中包含一个 {@link UnsupportedOperationException} 异常。
     * 子类可以重写该方法来实现自己的刷新令牌逻辑。
     *
     * @param refreshToken 用于刷新令牌的字符串
     * @return 包含刷新令牌结果的 {@link Mono}
     */
    default Mono<AuthToken> refresh(String refreshToken) {
        return Mono.error(new UnsupportedOperationException());
    }
}
