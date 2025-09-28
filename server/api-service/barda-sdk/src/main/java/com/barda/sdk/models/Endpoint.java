package com.barda.sdk.models;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 端点类，表示一个网络端点，包含主机和端口。
 */
@ToString
@EqualsAndHashCode
public class Endpoint {

    /**
     * 主机名
     */
    private final String host;

    /**
     * 端口号
     */
    private final Long port;

    /**
     * 构造函数，使用JSON反序列化时会用到。
     *
     * @param host 主机名
     * @param port 端口号
     */
    @JsonCreator
    public Endpoint(String host, Long port) {
        this.host = host;
        this.port = port;
    }

    /**
     * 获取端口号。
     *
     * @return 端口号
     */
    public Long getPort() {
        return port;
    }

    /**
     * 获取主机名，并去掉前后空格。
     *
     * @return 主机名
     */
    public String getHost() {
        return StringUtils.trimToEmpty(host);
    }

    /**
     * 获取端口号，如果端口号为null，则返回默认端口号。
     *
     * @param defaultPort 默认端口号
     * @return 端口号
     */
    public long getPort(long defaultPort) {
        return port == null ? defaultPort : port;
    }
}
