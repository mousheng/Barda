package com.barda.api.framework.security;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * 一个实现了ServerAccessDeniedHandler接口的组件类。
 * 该类用于处理访问被拒绝的情况。
 */
@Component
public class AccessDeniedHandler implements ServerAccessDeniedHandler {

    /**
     * 重写handle()方法，用于处理访问被拒绝的情况
     *
     * @param exchange  ServerWebExchange，表示一个HTTP请求和响应
     * @param denied    AccessDeniedException，表示访问被拒绝
     * @return  一个Mono<Void>，表示处理完成
     */
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {
        return Mono.fromRunnable(() -> {
            // 获取ServerHttpResponse
            ServerHttpResponse response = exchange.getResponse();
            // 设置HTTP状态码为401（未授权）
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
        });
    }

}
