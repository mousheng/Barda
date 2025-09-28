package com.barda.api.framework.service;

import java.util.Locale;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.reactive.function.server.ServerRequest;

/**
 * 全局上下文服务接口。
 * 定义了获取客户端区域设置的相关方法。
 */
public interface GlobalContextService {

    /**
     * 获取来自{@link ServerHttpRequest}的客户端区域设置。
     *
     * @param request 包含客户端请求的HTTP请求
     * @return 客户端的区域设置
     */
    Locale getClientLocale(ServerHttpRequest request);

    /**
     * 获取来自{@link ServerRequest}的客户端区域设置。
     *
     * @param request 包含客户端请求的请求
     * @return 客户端的区域设置
     */
    Locale getClientLocale(ServerRequest request);
}
