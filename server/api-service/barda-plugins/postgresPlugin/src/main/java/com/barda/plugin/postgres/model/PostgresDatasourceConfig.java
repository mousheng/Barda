package com.barda.plugin.postgres.model;

import static com.barda.sdk.util.ExceptionUtils.ofPluginException;
import static com.barda.sdk.util.JsonUtils.fromJson;
import static com.barda.sdk.util.JsonUtils.toJson;

import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.barda.sdk.exception.PluginCommonError;
import com.barda.sdk.plugin.common.sql.SqlBasedDatasourceConnectionConfig;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

/**
 * PostgreSQL 数据源配置类。
 * 继承自 SqlBasedDatasourceConnectionConfig，提供 PostgreSQL 特定的配置。
 */
@Slf4j
public class PostgresDatasourceConfig extends SqlBasedDatasourceConnectionConfig {

    private static final long DEFAULT_PORT = 5432L;

    /**
     * 构建器模式创建 PostgresDatasourceConfig 实例。
     */
    @Builder
    public PostgresDatasourceConfig(String database, String username, String password, String host, Long port, boolean usingSsl,
            String serverTimezone,
            boolean isReadonly, boolean enableTurnOffPreparedStatement, Map<String, Object> extParams) {
        super(database, username, password, host, port, usingSsl, serverTimezone, isReadonly, enableTurnOffPreparedStatement, extParams);
    }

    /**
     * 获取默认的 PostgreSQL 端口号。
     *
     * @return 默认的 PostgreSQL 端口号
     */
    @Override
    protected long defaultPort() {
        return DEFAULT_PORT;
    }

    /**
     * 从 Map 构建 PostgresDatasourceConfig 实例。
     *
     * @param requestMap 包含配置信息的 Map
     * @return 构建的 PostgresDatasourceConfig 实例
     * @throws PluginException 如果构建失败
     */
    public static PostgresDatasourceConfig buildFrom(Map<String, Object> requestMap) {
        PostgresDatasourceConfig result = fromJson(toJson(requestMap), PostgresDatasourceConfig.class);
        if (result == null) {
            throw ofPluginException(PluginCommonError.DATASOURCE_ARGUMENT_ERROR, "INVALID_PG_CONFIG");
        }
        return result;
    }

    /**
     * 获取 PostgresDatasourceConfig 的构建器。
     * 用于在测试中构建 PostgresDatasourceConfig 实例。
     *
     * @return PostgresDatasourceConfig 的构建器
     */
    @VisibleForTesting
    public PostgresDatasourceConfigBuilder toBuilder() {
        return builder()
                .database(getDatabase())
                .username(getUsername())
                .password(getPassword())
                .usingSsl(isUsingSsl())
                .host(getHost())
                .port(getPort())
                .serverTimezone(getServerTimezone())
                .enableTurnOffPreparedStatement(isEnableTurnOffPreparedStatement());
    }

}
