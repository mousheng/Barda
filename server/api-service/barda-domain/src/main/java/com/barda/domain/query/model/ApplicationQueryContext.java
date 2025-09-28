package com.barda.domain.query.model;

import com.barda.domain.application.model.Application;
import com.barda.domain.datasource.model.Datasource;

import lombok.Getter;
import reactor.core.publisher.Mono;

import lombok.Getter;
import reactor.core.publisher.Mono;

/**
 * 该类表示应用查询的上下文。
 * 它继承自 QueryContext 类，并添加了对 ApplicationQuery 和 Application 对象的反应式（Mono）引用。
 */
@Getter
public class ApplicationQueryContext extends QueryContext {

    /**
     * 应用查询的反应式（Mono）引用。
     */
    private final Mono<ApplicationQuery> applicationQueryMono;

    /**
     * 应用的反应式（Mono）引用。
     */
    private final Mono<Application> applicationMono;

    /**
     * 构造函数，用于创建 ApplicationQueryContext 实例。
     *
     * @param baseQueryMono  基础查询的反应式（Mono）引用
     * @param datasourceMono  数据源的反应式（Mono）引用
     * @param applicationQueryMono  应用查询的反应式（Mono）引用
     * @param applicationMono  应用的反应式（Mono）引用
     */
    public ApplicationQueryContext(Mono<BaseQuery> baseQueryMono, Mono<Datasource> datasourceMono,
            Mono<ApplicationQuery> applicationQueryMono, Mono<Application> applicationMono) {
        super(baseQueryMono, datasourceMono);
        this.applicationQueryMono = applicationQueryMono;
        this.applicationMono = applicationMono;
    }
}
