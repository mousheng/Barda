package com.barda.sdk.plugin.common;

import static com.barda.sdk.exception.PluginCommonError.DATASOURCE_TIMEOUT_ERROR;
import static com.barda.sdk.exception.PluginCommonError.QUERY_EXECUTION_ERROR;
import static com.barda.sdk.util.ExceptionUtils.ofPluginException;
import static com.barda.sdk.util.JsonUtils.fromJson;
import static com.barda.sdk.util.JsonUtils.toJson;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nonnull;

import org.pf4j.ExtensionPoint;

import com.google.common.reflect.TypeToken;
import com.barda.sdk.exception.PluginCommonError;
import com.barda.sdk.exception.PluginException;
import com.barda.sdk.models.DatasourceConnectionConfig;
import com.barda.sdk.models.DatasourceTestResult;
import com.barda.sdk.models.TokenBasedConnectionDetail;

import reactor.core.publisher.Mono;

/**
 * DatasourceConnector是一个数据源连接器接口，用于连接数据库。
 *
 * @param <Connection>        连接对象的类型
 * @param <ConnectionConfig>  连接配置对象的类型，必须是DatasourceConnectionConfig的子类
 */
@SuppressWarnings("unchecked")
public interface DatasourceConnector<Connection, ConnectionConfig extends DatasourceConnectionConfig> extends ExtensionPoint {

    /**
     * 根据配置参数解析连接配置。
     *
     * @param configMap 配置参数
     * @return 解析后的连接配置对象
     */
    @SuppressWarnings("UnstableApiUsage")
    @Nonnull
    default ConnectionConfig resolveConfig(Map<String, Object> configMap) {
        TypeToken<ConnectionConfig> type = new TypeToken<>(getClass()) {
        };

        Class<? super ConnectionConfig> tClass = type.getRawType();
        Object result = fromJson(toJson(configMap), tClass);
        if (result == null) {
            throw ofPluginException(PluginCommonError.DATASOURCE_ARGUMENT_ERROR, "DATASOURCE_CONFIG_ERROR");
        }
        return (ConnectionConfig) result;
    }

    /**
     * 不应该重写此方法！
     *
     * @param config 数据源连接配置
     * @return 数据源验证消息集合
     */
    default Set<String> doValidateConfig(DatasourceConnectionConfig config) {
        ConnectionConfig connectionConfig;
        try {
            connectionConfig = (ConnectionConfig) config;
        } catch (ClassCastException e) {
            throw ofPluginException(PluginCommonError.INVALID_QUERY_SETTINGS, "DATASOURCE_TYPE_ERROR", e.getMessage());
        }

        return validateConfig(connectionConfig);
    }

    /**
     * 不要重写此方法！
     * 验证数据源连接配置。
     *
     * @param config 数据源连接配置
     * @return 数据源验证消息集合
     * @throws PluginException 如果配置类型无效，抛出插件异常
     */
    default Mono<DatasourceTestResult> doTestConnection(DatasourceConnectionConfig config) {
        ConnectionConfig connectionConfig;
        try {
            connectionConfig = (ConnectionConfig) config;
        } catch (ClassCastException e) {
            throw ofPluginException(PluginCommonError.DATASOURCE_ARGUMENT_ERROR, "DATASOURCE_TYPE_ERROR", e.getMessage());
        }
        return testConnection(connectionConfig);
    }

    /**
     * 执行数据库连接创建。
     *
     * @param config 连接配置对象
     * @return 表示连接的Mono对象
     * @deprecated 不建议重写此方法
     */
    default Mono<Connection> doCreateConnection(DatasourceConnectionConfig config) {
        ConnectionConfig connectionConfig;
        try {
            connectionConfig = (ConnectionConfig) config;
        } catch (ClassCastException e) {
            throw ofPluginException(PluginCommonError.INVALID_QUERY_SETTINGS, "DATASOURCE_TYPE_ERROR", e.getMessage());
        }

        return createConnection(connectionConfig)
                .timeout(Duration.ofSeconds(10))
                .onErrorMap(TimeoutException.class, error -> new PluginException(DATASOURCE_TIMEOUT_ERROR, "DATASOURCE_TIMEOUT_ERROR"))
                .onErrorMap(Throwable.class, error -> {
                    if (error instanceof PluginException) {
                        return error;
                    }
                    return new PluginException(QUERY_EXECUTION_ERROR, "PLUGIN_CREATE_CONNECTION_FAILED", error.getMessage());
                });
    }

    Set<String> validateConfig(ConnectionConfig config);

    Mono<DatasourceTestResult> testConnection(ConnectionConfig config);

    Mono<Connection> createConnection(ConnectionConfig connectionConfig);

    Mono<Void> destroyConnection(Connection connection);

    /**
     * 解析Token连接详情。
     *
     * @param tokenDetail Token详情
     * @return Token连接详情对象
     */
    default TokenBasedConnectionDetail resolveTokenDetail(Map<String, Object> tokenDetail) {
        throw new UnsupportedOperationException();
    }
}
