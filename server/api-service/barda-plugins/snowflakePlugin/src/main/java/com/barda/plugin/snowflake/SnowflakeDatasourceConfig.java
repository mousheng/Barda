package com.barda.plugin.snowflake;

import java.util.Map;

import com.barda.sdk.plugin.common.sql.SqlBasedDatasourceConnectionConfig;

import lombok.Builder;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * 用于存储和表示Snowflake数据源配置的类。
 * 该类继承自SqlBasedDatasourceConnectionConfig，并提供Snowflake数据源的相关配置。
 */
@ToString
@Slf4j
public class SnowflakeDatasourceConfig extends SqlBasedDatasourceConnectionConfig {

    /**
     * 构造函数。
     *
     * @param database 数据库名称
     * @param username 用户名
     * @param password 密码
     * @param host 主机名
     * @param port 端口号
     * @param usingSsl 是否使用SSL
     * @param serverTimezone 服务器时区
     * @param isReadonly 是否为只读模式
     * @param enableTurnOffPreparedStatement 是否禁用PreparedStatement
     * @param extParams 扩展参数
     */
    @Builder
    public SnowflakeDatasourceConfig(String database, String username, String password, String host,
            Long port, boolean usingSsl, String serverTimezone,
            boolean isReadonly, boolean enableTurnOffPreparedStatement, Map<String, Object> extParams) {
        super(database, username, password, host, port, usingSsl, serverTimezone, isReadonly, enableTurnOffPreparedStatement, extParams);
    }

    /**
     * 获取默认的端口号。
     * 对于Snowflake数据源，默认的端口号为-1。
     *
     * @return 默认的端口号
     */
    @Override
    protected long defaultPort() {
        return -1;
    }

}