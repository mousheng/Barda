package com.barda.api.authentication.request;

import static com.barda.api.authentication.util.AuthenticationUtils.AUTH_REQUEST_THREAD_POOL;

import com.barda.domain.authentication.context.AuthRequestContext;
import com.barda.domain.user.model.AuthUser;

import reactor.core.publisher.Mono;

/**
 * 定义了阻塞式认证请求的抽象类。
 * 子类需要实现 {@link #authSync(AuthRequestContext)} 方法来执行同步的认证操作。
 */
public abstract class AbstractBlockedAuthRequest implements AuthRequest {

    /**
     * 重写了 {@link AuthRequest#auth(AuthRequestContext)} 方法，
     * 并使用 {@link Mono#fromSupplier(java.util.function.Supplier)} 包装了同步的认证操作，
     * @param authRequestContext 认证请求上下文
     * @return 包含认证结果的 {@link Mono}
     */
    @Override
    public final Mono<AuthUser> auth(AuthRequestContext authRequestContext) {
        return Mono.fromSupplier(() -> authSync(authRequestContext))
                .subscribeOn(AUTH_REQUEST_THREAD_POOL);
    }

    /**
     * 子类需要实现的同步的认证操作。
     *
     * @param authRequestContext 认证请求上下文
     * @return 认证结果
     */
    protected abstract AuthUser authSync(AuthRequestContext authRequestContext);
}
