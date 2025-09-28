package com.barda.infra.util;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import com.barda.sdk.util.JsonUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 一个提供与网络相关的实用工具类。
 */
@Slf4j
public class NetworkUtils {

    /**
     * 获取来自ServerWebExchange的远程IP。
     *
     * 首先检查X-Real-IP头部，如果存在，则返回该值。
     * 其次检查RemoteIp头部，如果存在，则返回该值。
     * 如果以上头部都不存在，则从远程地址中获取IP。
     *
     * @param serverWebExchange 包含请求信息的ServerWebExchange
     * @return 远程IP
     */
    public static String getRemoteIp(ServerWebExchange serverWebExchange) {
        ServerHttpRequest request = serverWebExchange.getRequest();
        HttpHeaders headers = request.getHeaders();
        String xRealIp = headers.getFirst("X-Real-IP");
        if (StringUtils.isNotBlank(xRealIp)) {
            return xRealIp;
        }
        String remoteIp = headers.getFirst("RemoteIp");
        if (StringUtils.isNotBlank(remoteIp)) {
            return remoteIp;
        }
        log.debug("get remote ip from remoteAddress, header {}", JsonUtils.toJson(headers));
        return Optional.ofNullable(serverWebExchange.getRequest().getRemoteAddress())
               .map(InetSocketAddress::getAddress)
               .map(InetAddress::getHostAddress)
               .orElse("");
    }
}
