package com.barda.plugin.sql;

import static com.barda.sdk.exception.PluginCommonError.QUERY_EXECUTION_ERROR;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;

import com.barda.sdk.exception.PluginException;
import com.barda.sdk.models.DatasourceTestResult;
import com.barda.sdk.plugin.common.BlockingDatasourceConnector;
import com.barda.sdk.plugin.common.sql.HikariPerfWrapper;
import com.barda.sdk.plugin.common.sql.SqlBasedDatasourceConnectionConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * 基于SQL的连接器的抽象类。
 * 该类提供了一个创建、测试和销毁SQL数据源连接的基础实现。
 *
 * @param <T> 继承自SqlBasedDatasourceConnectionConfig的连接配置类
 */
public abstract class SqlBasedConnector<T extends SqlBasedDatasourceConnectionConfig> extends BlockingDatasourceConnector<HikariPerfWrapper, T> {

    private static final long LEAK_DETECTION_THRESHOLD_MS = TimeUnit.SECONDS.toMillis(30);
    private static final long MAX_LIFETIME_MS = TimeUnit.MINUTES.toMillis(30);
    private static final long KEEPALIVE_TIME_MS = TimeUnit.MINUTES.toMillis(3);
    private static final long CONNECTION_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(5);
    private static final long VALIDATION_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(3);
    private static final long INITIALIZATION_FAIL_TIMEOUT = TimeUnit.SECONDS.toMillis(4);
    private static final long CONNECTION_POOL_IDLE_TIMEOUT_MILLIS = TimeUnit.MINUTES.toMillis(5);
    private final int connectionPoolMaxPoolSize;

    /**
     * 构造函数。
     *
     * @param connectionPoolMaxPoolSize 连接池的最大连接数
     */
    protected SqlBasedConnector(int connectionPoolMaxPoolSize) {
        this.connectionPoolMaxPoolSize = connectionPoolMaxPoolSize;
    }

    /**
     * 创建一个阻塞的SQL数据源连接。
     *
     * @param connectionConfig 连接配置
     * @return 包装了HikariDataSource的HikariPerfWrapper
     */
    @Nonnull
    @Override
    protected final HikariPerfWrapper blockingCreateConnection(T connectionConfig) {
        try {
            Class.forName(getJdbcDriver());
        } catch (ClassNotFoundException e) {
            throw new PluginException(QUERY_EXECUTION_ERROR, "LOAD_SQL_JDBC_ERROR");
        }

        HikariConfig config = new HikariConfig();
        config.setDriverClassName(getJdbcDriver());
        config.setMinimumIdle(1);
        config.setMaxLifetime(MAX_LIFETIME_MS);
        config.setKeepaliveTime(KEEPALIVE_TIME_MS);
        config.setIdleTimeout(CONNECTION_POOL_IDLE_TIMEOUT_MILLIS);
        config.setMaximumPoolSize(connectionPoolMaxPoolSize);
        config.setLeakDetectionThreshold(LEAK_DETECTION_THRESHOLD_MS);
        config.setConnectionTimeout(CONNECTION_TIMEOUT_MS);
        config.setValidationTimeout(VALIDATION_TIMEOUT_MS);
        config.setInitializationFailTimeout(INITIALIZATION_FAIL_TIMEOUT);

        setUpConfigs(connectionConfig, config);

        connectionConfig.getExtParams()
                .forEach((key, value) -> {
                    if (StringUtils.isBlank(key) || value == null || StringUtils.isBlank(String.valueOf(value))) {
                        return;
                    }
                    config.addDataSourceProperty(key, value);
                });

        HikariDataSource hikariDataSource = new HikariDataSource(config);
        return HikariPerfWrapper.wrap(hikariDataSource,
                () -> hikariDataSource.getHikariPoolMXBean().getTotalConnections(),
                () -> hikariDataSource.getHikariPoolMXBean().getIdleConnections(),
                () -> hikariDataSource.getHikariPoolMXBean().getActiveConnections(),
                () -> hikariDataSource.getHikariPoolMXBean().getThreadsAwaitingConnection(),
                hikariDataSource::getDataSourceProperties,
                hikariDataSource::getHealthCheckProperties
        );
    }

    /**
     * 获取JDBC驱动的类名。
     *
     * @return JDBC驱动的类名
     */
    protected abstract String getJdbcDriver();

    /**
     * 设置HikariCP连接池的配置。
     *
     * @param datasourceConfig 连接配置
     * @param config HikariCP的配置
     */
    protected abstract void setUpConfigs(T datasourceConfig, HikariConfig config);

    /**
     * 测试SQL数据源连接是否可用。
     *
     * @param wrapper 包装了HikariDataSource的HikariPerfWrapper
     * @return 测试结果
     */
    @Nonnull
    @Override
    protected final DatasourceTestResult blockingTestConnection(HikariPerfWrapper wrapper) {
        blockingDestroyConnection(wrapper);
        return DatasourceTestResult.testSuccess();
    }

    /**
     * 销毁SQL数据源连接。
     *
     * @param wrapper 包装了HikariDataSource的HikariPerfWrapper
     */
    @Override
    protected final void blockingDestroyConnection(HikariPerfWrapper wrapper) {
        if (wrapper != null) {
            ((HikariDataSource) wrapper.getHikariDataSource()).close();
        }
    }

    /**
     * 验证连接配置是否有效。
     *
     * @param connectionConfig 连接配置
     * @return 无效的配置项
     */
    @Override
    public Set<String> validateConfig(T connectionConfig) {

        Set<String> invalids = new HashSet<>();

        String host = connectionConfig.getHost();
        if (StringUtils.isBlank(host)) {
            invalids.add("HOST_EMPTY_PLZ_CHECK");
        }

        if (host.contains("/") || host.contains(":")) {
            invalids.add("HOST_WITH_COLON");
        }

        if (StringUtils.equalsIgnoreCase(host, "localhost") || StringUtils.equals(host, "127.0.0.1")) {
            invalids.add("INVALID_HOST");
        }

        if (StringUtils.isBlank(connectionConfig.getDatabase())) {
            invalids.add("DATABASE_NAME_EMPTY");
        }

        return invalids;
    }
}
