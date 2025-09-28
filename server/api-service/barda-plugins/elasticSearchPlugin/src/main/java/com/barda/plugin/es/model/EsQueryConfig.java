package com.barda.plugin.es.model;

import org.springframework.http.HttpMethod;

import lombok.Builder;

/**
 * 用于配置 Elasticsearch 查询的类。
 * 该类使用 Lombok 的 {@link Builder} 注解来生成构建器模式的构造函数。
 */
@Builder
public class EsQueryConfig {

    /**
     * 由客户端使用，表示 Elasticsearch 操作方法。
     */
    private String esMethod;

    /**
     * 路径前缀。
     */
    private String prefix;

    /**
     * 路径后缀。
     */
    private String suffix;

    /**
     * 路径。
     */
    private String path;

    /**
     * HTTP 方法。
     */
    private HttpMethod httpMethod;

    /**
     * Elasticsearch 查询 DSL。
     */
    private String dsl;

    /**
     * 获取 HTTP 方法。
     *
     * @return HTTP 方法
     */
    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    /**
     * 设置 HTTP 方法。
     *
     * @param httpMethod HTTP 方法
     */
    public void setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

    /**
     * 获取路径。
     *
     * @return 路径
     */
    public String getPath() {
        return path;
    }

    /**
     * 设置路径。
     *
     * @param path 路径
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 获取 Elasticsearch 查询 DSL。
     *
     * @return Elasticsearch 查询 DSL
     */
    public String getDsl() {
        return dsl;
    }

    /**
     * 设置 Elasticsearch 查询 DSL。
     *
     * @param dsl Elasticsearch 查询 DSL
     */
    public void setDsl(String dsl) {
        this.dsl = dsl;
    }

    /**
     * 获取路径前缀。
     *
     * @return 路径前缀
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * 设置路径前缀。
     *
     * @param prefix 路径前缀
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * 获取路径后缀。
     *
     * @return 路径后缀
     */
    public String getSuffix() {
        return suffix;
    }

    /**
     * 设置路径后缀。
     *
     * @param suffix 路径后缀
     */
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    /**
     * 获取 Elasticsearch 操作方法。
     *
     * @return Elasticsearch 操作方法
     */
    public String getEsMethod() {
        return esMethod;
    }

    /**
     * 设置 Elasticsearch 操作方法。
     *
     * @param esMethod Elasticsearch 操作方法
     */
    public void setEsMethod(String esMethod) {
        this.esMethod = esMethod;
    }

}
