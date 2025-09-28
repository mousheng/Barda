package com.barda.api.authentication.request;

import java.util.Set;

import reactor.core.publisher.Mono;

import com.barda.domain.authentication.context.AuthRequestContext;

/**
 * 定义了构建认证请求的工厂接口。
 * 该接口包含了构建 {@link AuthRequest} 并返回支持的认证类型的方法。
 *
 * @param <T> 认证请求上下文的类型
 */
public interface AuthRequestFactory<T extends AuthRequestContext> {

    /**
     * 构建一个 {@link AuthRequest}。
     *
     * @param context 认证请求上下文
     * @return 包含构建的 {@link AuthRequest} 的 {@link Mono}
     */
    Mono<AuthRequest> build(T context);

    /**
     * 返回该工厂所支持的认证类型。
     *
     * @return 包含支持的认证类型的 {@link Set}
     */
    Set<String> supportedAuthTypes();
}
