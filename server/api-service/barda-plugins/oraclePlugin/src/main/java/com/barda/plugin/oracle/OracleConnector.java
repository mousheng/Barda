package com.barda.plugin.oracle;

import static org.apache.commons.lang3.StringUtils.isAllBlank;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.pf4j.Extension;

import com.barda.plugin.oracle.model.OracleDatasourceConfig;
import com.barda.plugin.sql.SqlBasedConnector;
import com.zaxxer.hikari.HikariConfig;

/**
 * OracleConnector 类是 SqlBasedConnector 的扩展类，
 * 用于表示 Oracle 数据库的连接器。
 * 它使用了 Lombok 的 @Extension 注解来标记为扩展类。
 */
@Extension
public class OracleConnector extends SqlBasedConnector<OracleDatasourceConfig> {

    private static final String JDBC_DRIVER = "oracle.jdbc.OracleDriver";

    /**
     * 公共构造函数，
     * 用于初始化 OracleConnector 实例。
     *
     * @param maxPoolSize 最大连接池大小
     */
    public OracleConnector() {
        super(50);
    }

    /**
     * 获取 JDBC 驱动。
     *
     * @return JDBC 驱动
     */
    @Override
    protected String getJdbcDriver() {
        return JDBC_DRIVER;
    }

    /**
     * 重写 setUpConfigs 方法，
     * 用于设置 HikariConfig。
     *
     * @param oracleDatasourceConfig Oracle 数据源配置
     * @param config HikariConfig
     */
    @Override
    protected void setUpConfigs(OracleDatasourceConfig oracleDatasourceConfig, HikariConfig config) {
        config.setDriverClassName(JDBC_DRIVER);
        // 设置用户名和密码
        if (StringUtils.isNotBlank(oracleDatasourceConfig.getUsername())) {
            config.setUsername(oracleDatasourceConfig.getUsername());
        }
        if (StringUtils.isNotBlank(oracleDatasourceConfig.getPassword())) {
            config.setPassword(oracleDatasourceConfig.getPassword());
        }

        // 设置 JDBC URL
        config.setJdbcUrl(oracleDatasourceConfig.getJdbcUrl());

        // 设置只读模式
        config.setReadOnly(oracleDatasourceConfig.isReadonly());
    }

    /**
     * 重写 validateConfig 方法，
     * 用于验证 Oracle 数据源配置。
     *
     * @param connectionConfig Oracle 数据源配置
     * @return 验证结果
     */
    @Override
    public Set<String> validateConfig(OracleDatasourceConfig connectionConfig) {
        Set<String> validates = new HashSet<>();

        // 验证 JDBC URL 配置
        if (isBlank(connectionConfig.getJdbcUrl())
                && (isBlank(connectionConfig.getHost()) || isAllBlank(connectionConfig.getSid(), connectionConfig.getServiceName()))) {
            validates.add("INVALID_JDBC_URL_CONFIG");
        }

        return validates;
    }
}
