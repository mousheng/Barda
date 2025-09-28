package com.barda.infra.js;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.barda.sdk.config.CommonConfigHelper;

/**
 * 一个提供与节点服务器相关的帮助功能的类。
 * 该类使用 Spring 的 {@link Component} 注解来使其可以被 Spring 容器管理。
 */
@Component
public class NodeServerHelper {

    /**
     * 与节点服务器 API 相关的路径前缀。
     */
    private static final String PREFIX = "node-service/api";

    /**
     * 一个用于获取通用配置的帮助类。
     * 该类使用 Spring 的 {@link Autowired} 注解来注入。
     */
    @Autowired
    private CommonConfigHelper commonConfigHelper;

    /**
     * 创建一个新的 {@link URI}，该 URI 包含了节点服务器的主机和指定的路径。
     *
     * @param path 要添加到 URI 中的路径
     * @return 一个新的 {@link URI}
     */
    public URI createUri(String path) {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return UriComponentsBuilder.fromUriString(commonConfigHelper.getHost())
               .pathSegment(PREFIX, path)
               .build()
               .toUri();
    }
}
