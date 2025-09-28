package com.barda.sdk.util;


import java.net.URI;
import java.util.Optional;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import com.google.common.net.InternetDomainName;
import com.barda.sdk.constants.GlobalContext;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * UriUtils 类提供 URI 相关的实用工具。
 * 该类包含了从 ServerWebExchange 获取 Referer 域、解析 Referer URI、获取顶级私有域等方法。
 */
@Slf4j
public class UriUtils {

    public static final String REFERER = "Referer";
    private static final String LOCALHOST = "localhost";

    /**
     * 从 ServerWebExchange 获取 Referer 域。
     *
     * @param exchange ServerWebExchange 对象
     * @return Referer 域
     */
    public static String getRefererDomainFromRequest(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        return Optional.ofNullable(getRefererURI(request))
                .map(URI::getHost)
                .map(String::toLowerCase)
                .orElse("");
    }

    /**
     * 获取上下文中的 Referer 域。
     *
     * @return Referer 域
     */
    public static Mono<String> getRefererDomainFromContext() {
        return Mono.deferContextual(contextView -> {
            String domain = contextView.getOrDefault(GlobalContext.DOMAIN, null);
            if (domain == null) {
                return Mono.empty();
            }
            return Mono.just(domain);
        });
    }

    /**
     * 获取 Referer URI。
     *
     * @param request ServerHttpRequest 对象
     * @return Referer URI
     */
    @Nullable
    public static URI getRefererURI(ServerHttpRequest request) {
        String refer = request.getHeaders().getFirst(REFERER);
        if (StringUtils.isNotBlank(refer)) {
            return URI.create(refer);
        }
        return null;
    }

    /**
     * 获取 URI 的顶级私有域。
     *
     * @param exchange ServerWebExchange 对象
     * @return 顶级私有域
     */
    @SuppressWarnings("UnstableApiUsage")
    public static String getTopPrivateDomain(ServerWebExchange exchange) {
        URI uri = exchange.getRequest().getURI();
        try {
            if (InternetDomainName.isValid(uri.getHost()) && !LOCALHOST.equalsIgnoreCase(uri.getHost())) {
                return InternetDomainName.from(uri.getHost()).topPrivateDomain().toString().toLowerCase();
            }
        } catch (Exception e) {
            log.error("get top private domain error", e);
        }
        return uri.getHost().toLowerCase();
    }
}
