package com.barda.sdk.plugin.mysql;

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
 * 表示MySQL数据源配置类，继承自基于SQL的数据源连接配置。
 */
@Slf4j
public class MysqlDatasourceConfig extends SqlBasedDatasourceConnectionConfig {

    /**
     * 默认的MySQL端口号
     */
    private static final long DEFAULT_PORT = 3306L;

    /**
     * 构造一个新的MySQL数据源配置对象。
     *
     * @param database                      数据库名称
     * @param username                      用户名
     * @param password                      密码
     * @param host                          主机地址
     * @param port                          端口号
     * @param usingSsl                      是否使用SSL
     * @param serverTimezone                服务器时区
     * @param isReadonly                    是否只读
     * @param enableTurnOffPreparedStatement 是否禁用预编译语句
     * @param extParams                     扩展参数
     */
    @Builder
    public MysqlDatasourceConfig(String database, String username, String password, String host,
            Long port, boolean usingSsl, String serverTimezone,
            boolean isReadonly, boolean enableTurnOffPreparedStatement, Map<String, Object> extParams) {
        super(database, username, password, host, port, usingSsl, serverTimezone, isReadonly, enableTurnOffPreparedStatement, extParams);
    }

    /**
     * 获取默认的MySQL端口号。
     *
     * @return 默认端口号
     */
    @Override
    protected long defaultPort() {
        return DEFAULT_PORT;
    }

    /**
     * 从请求映射构建MySQL数据源配置对象。
     *
     * @param requestMap 请求映射
     * @return 构建的MySQL数据源配置对象
     * @throws PluginException 如果配置无效，抛出插件异常
     */
    public static MysqlDatasourceConfig buildFrom(Map<String, Object> requestMap) {
        MysqlDatasourceConfig result = fromJson(toJson(requestMap), MysqlDatasourceConfig.class);
        if (result == null) {
            throw ofPluginException(PluginCommonError.DATASOURCE_ARGUMENT_ERROR, "INVALID_MYSQL_CONFIG");
        }
        return result;
    }

    /**
     * 为测试可见，将当前配置转换为构建器对象。
     *
     * @return 当前配置的构建器对象
     */
    @VisibleForTesting
    public MysqlDatasourceConfigBuilder toBuilder() {
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