package com.barda.sdk.plugin.common;

import static com.barda.sdk.plugin.common.QueryExecutionUtils.querySharedScheduler;

import javax.annotation.Nonnull;

import com.barda.sdk.models.DatasourceConnectionConfig;
import com.barda.sdk.models.DatasourceTestResult;

import reactor.core.publisher.Mono;

/**
 * BlockingDatasourceConnector是一个阻塞式数据源连接器的抽象实现。
 *
 * @param <Connection>        连接对象的类型
 * @param <ConnectionConfig>  连接配置对象的类型，必须是DatasourceConnectionConfig的子类
 */
public abstract class BlockingDatasourceConnector<Connection, ConnectionConfig extends DatasourceConnectionConfig>
        implements DatasourceConnector<Connection, ConnectionConfig> {

    /**
     * 创建数据库连接。
     *
     * @param connectionConfig 连接配置
     * @return 表示连接的Mono对象
     */
    @Override
    public final Mono<Connection> createConnection(ConnectionConfig connectionConfig) {
        return Mono.fromSupplier(() -> blockingCreateConnection(connectionConfig))
                .subscribeOn(querySharedScheduler());
    }

    /**
     * 测试数据库连接是否可用。
     *
     * @param connectionConfig 连接配置
     * @return 表示连接测试结果的Mono对象
     */
    @Override
    public final Mono<DatasourceTestResult> testConnection(ConnectionConfig connectionConfig) {
        return Mono.fromSupplier(() -> blockingTestConnection(blockingCreateConnection(connectionConfig)))
                .subscribeOn(querySharedScheduler());
    }

    /**
     * 销毁数据库连接。
     *
     * @param connection 数据库连接
     * @return 表示销毁连接操作的Mono对象
     */
    @Override
    public final Mono<Void> destroyConnection(Connection connection) {
        return Mono.fromRunnable(() -> blockingDestroyConnection(connection))
                .subscribeOn(querySharedScheduler())
                .then();
    }

    /**
     * 阻塞式测试数据库连接是否可用。
     *
     * @param connection 数据库连接
     * @return 数据源测试结果
     */
    @Nonnull
    protected abstract DatasourceTestResult blockingTestConnection(Connection connection);

    /**
     * 阻塞式销毁数据库连接。
     *
     * @param connection 数据库连接
     */
    protected abstract void blockingDestroyConnection(Connection connection);

    /**
     * 阻塞式创建数据库连接。
     *
     * @param connectionConfig 连接配置
     * @return 数据库连接对象
     */
    @Nonnull
    protected abstract Connection blockingCreateConnection(ConnectionConfig connectionConfig);
}
