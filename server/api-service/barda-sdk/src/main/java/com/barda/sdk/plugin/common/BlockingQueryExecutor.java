package com.barda.sdk.plugin.common;

import static com.barda.sdk.plugin.common.QueryExecutionUtils.querySharedScheduler;

import javax.annotation.Nonnull;

import com.barda.sdk.models.DatasourceConnectionConfig;
import com.barda.sdk.models.DatasourceStructure;
import com.barda.sdk.models.QueryExecutionResult;
import com.barda.sdk.query.QueryExecutionContext;

import reactor.core.publisher.Mono;

/**
 * BlockingQueryExecutor是一个阻塞式查询执行器的抽象实现。
 *
 * @param <ConnectionConfig> 连接配置对象的类型，必须是DatasourceConnectionConfig的子类
 * @param <Connection>       连接对象的类型
 * @param <QueryContext>     查询执行上下文对象的类型，必须是QueryExecutionContext的子类
 */
public abstract class BlockingQueryExecutor<ConnectionConfig extends DatasourceConnectionConfig, Connection,
        QueryContext extends QueryExecutionContext>
        implements QueryExecutor<ConnectionConfig, Connection, QueryContext> {

    /**
     * 执行查询。
     *
     * @param connection            数据库连接
     * @param queryExecutionContext 查询执行上下文
     * @return 表示查询结果的Mono对象
     */
    @Override
    public final Mono<QueryExecutionResult> executeQuery(Connection connection, QueryContext queryExecutionContext) {
        return Mono.fromSupplier(() -> blockingExecuteQuery(connection, queryExecutionContext))
                .subscribeOn(querySharedScheduler());
    }

    /**
     * 获取数据源结构信息。
     *
     * @param connection       数据库连接
     * @param connectionConfig 连接配置
     * @return 表示数据源结构信息的Mono对象
     */
    @Override
    public final Mono<DatasourceStructure> getStructure(Connection connection, ConnectionConfig connectionConfig) {
        return Mono.fromCallable(() -> blockingGetStructure(connection, connectionConfig))
                .subscribeOn(querySharedScheduler());
    }

    /**
     * 阻塞式执行查询。
     *
     * @param connection       数据库连接
     * @param context          查询执行上下文
     * @return 查询执行结果
     */
    @Nonnull
    protected abstract QueryExecutionResult blockingExecuteQuery(Connection connection, QueryContext context);

    /**
     * 阻塞式获取数据源结构信息。
     *
     * @param connection       数据库连接
     * @param connectionConfig 连接配置
     * @return 数据源结构信息
     */
    @SuppressWarnings("unused")
    @Nonnull
    protected DatasourceStructure blockingGetStructure(Connection connection, 
            ConnectionConfig connectionConfig) {
        return new DatasourceStructure(); // 虚拟结果
    }
}
