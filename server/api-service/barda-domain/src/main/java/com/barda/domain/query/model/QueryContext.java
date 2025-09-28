package com.barda.domain.query.model;

import com.barda.domain.datasource.model.Datasource;

import lombok.Getter;
import reactor.core.publisher.Mono;

/**
 * 查询上下文类，用于在执行查询时存储和传递相关数据。
 * 它包含了Mono类型的数据源和基础查询。
 */
@Getter
public class QueryContext {

    /**
     * 基础查询的Mono类型数据。
     * 它表示一个异步操作，返回一个BaseQuery对象。
     */
    private final Mono<BaseQuery> baseQueryMono;

    /**
     * 数据源的Mono类型数据。
     * 它表示一个异步操作，返回一个Datasource对象。
     */
    private final Mono<Datasource> datasourceMono;

    /**
     * 查询上下文类的构造函数。
     *
     * @param baseQueryMono 基础查询的Mono类型数据
     * @param datasourceMono 数据源的Mono类型数据
     */
    public QueryContext(Mono<BaseQuery> baseQueryMono, Mono<Datasource> datasourceMono) {
        this.baseQueryMono = baseQueryMono;
        this.datasourceMono = datasourceMono;
    }
}
