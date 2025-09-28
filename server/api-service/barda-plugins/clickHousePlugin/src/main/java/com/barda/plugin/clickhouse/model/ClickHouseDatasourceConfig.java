package com.barda.plugin.clickhouse.model;

import static com.barda.sdk.exception.PluginCommonError.DATASOURCE_ARGUMENT_ERROR;
import static com.barda.sdk.util.ExceptionUtils.ofPluginException;
import static com.barda.sdk.util.JsonUtils.fromJson;
import static com.barda.sdk.util.JsonUtils.toJson;

import java.util.Map;

import com.barda.sdk.plugin.common.sql.SqlBasedDatasourceConnectionConfig;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

/**
 * ClickHouseDatasourceConfig 类是 SqlBasedDatasourceConnectionConfig 的子类，
 * 用于表示 ClickHouse 数据源的配置。
 */
@Slf4j
public class ClickHouseDatasourceConfig extends SqlBasedDatasourceConnectionConfig {

    private static final long DEFAULT_PORT = 8123L;

    /**
     * 构建 ClickHouseDatasourceConfig 类的构造函数。
     *
     * @param database 数据库名称
     * @param username 用户名
     * @param password 密码
     * @param host 主机
     * @param port 端口
     * @param usingSsl 是否使用 SSL
     * @param serverTimezone 服务器时区
     * @param isReadonly 是否只读
     * @param enableTurnOffPreparedStatement 是否启用关闭 PreparedStatement 的功能
     * @param extParams 扩展参数
     */
    @Builder
    public ClickHouseDatasourceConfig(String database, String username, String password, String host, Long port, boolean usingSsl,
            String serverTimezone, boolean isReadonly, boolean enableTurnOffPreparedStatement, Map<String, Object> extParams) {
        super(database, username, password, host, port, usingSsl, serverTimezone, isReadonly, enableTurnOffPreparedStatement, extParams);
    }

    /**
     * 获取默认的端口号。
     *
     * @return 默认的端口号
     */
    @Override
    protected long defaultPort() {
        return DEFAULT_PORT;
    }

    /**
     * 从 Map 中构建 ClickHouseDatasourceConfig 类的实例。
     *
     * @param requestMap 请求参数的 Map
     * @return ClickHouseDatasourceConfig 类的实例
     * @throws PluginException 如果构建失败
     */
    public static ClickHouseDatasourceConfig buildFrom(Map<String, Object> requestMap) {
        ClickHouseDatasourceConfig result = fromJson(toJson(requestMap), ClickHouseDatasourceConfig.class);
        if (result == null) {
            throw ofPluginException(DATASOURCE_ARGUMENT_ERROR, "INVALID_CLICKHOUSE_CONFIG");
        }
        return result;
    }
}
