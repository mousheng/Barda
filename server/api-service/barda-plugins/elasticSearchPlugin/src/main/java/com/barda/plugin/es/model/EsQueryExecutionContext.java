package com.barda.plugin.es.model;

import org.springframework.http.HttpMethod;

import com.barda.sdk.query.QueryExecutionContext;

import lombok.Builder;

/**
 * 扩展自 {@link QueryExecutionContext} 的 Elasticsearch 查询执行上下文类。
 * 该类使用 Lombok 的 {@link Builder} 注解来生成构建器模式的构造函数。
 */
@Builder
public class EsQueryExecutionContext extends QueryExecutionContext {

    /**
     * HTTP 方法。
     */
    private HttpMethod httpMethod;

    /**
     * 路径。
     */
    private String path;

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

}
