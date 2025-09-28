package com.barda.api.framework.security;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;


/**
 * 一个实现了ServerAuthenticationEntryPoint接口的组件类。
 * 该类用于处理认证入口点的情况。
 */
@Component
public class AuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    /**
     * 重写commence()方法，用于处理认证入口点的情况
     *
     * @param exchange  ServerWebExchange，表示一个HTTP请求和响应
     * @param e         AuthenticationException，表示认证失败
     * @return  一个Mono<Void>，表示处理完成
     */
    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException e) {
        return Mono.fromRunnable(() -> {
            // 获取ServerHttpResponse
            ServerHttpResponse response = exchange.getResponse();
            // 设置HTTP状态码为401（未授权）
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
        });
    }
}
