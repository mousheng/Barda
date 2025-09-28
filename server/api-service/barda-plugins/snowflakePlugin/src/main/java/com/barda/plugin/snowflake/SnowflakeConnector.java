package com.barda.plugin.snowflake;

import org.pf4j.Extension;

import com.barda.plugin.sql.SqlBasedConnector;
import com.zaxxer.hikari.HikariConfig;

/**
 * 基于Snowflake的SQL连接器。
 * 该类继承自SqlBasedConnector，并实现了对Snowflake数据源的连接和操作。
 */
@Extension
public class SnowflakeConnector extends SqlBasedConnector<SnowflakeDatasourceConfig> {

    private static final String JDBC_DRIVER = "net.snowflake.client.jdbc.SnowflakeDriver";

    public SnowflakeConnector() {
        super(50);
    }

    /**
     * 获取JDBC驱动的类名。
     *
     * @return JDBC驱动的类名
     */
    @Override
    protected String getJdbcDriver() {
        return JDBC_DRIVER;
    }

    /**
     * 设置HikariCP连接池的配置。
     *
     * @param datasourceConfig 数据源配置
     * @param config HikariCP的配置
     */
    @Override
    protected void setUpConfigs(SnowflakeDatasourceConfig datasourceConfig, HikariConfig config) {
        String host = datasourceConfig.getHost();
        String database = datasourceConfig.getDatabase();

        String url = "jdbc:snowflake://" + host + ".snowflakecomputing.com/";
        config.setJdbcUrl(url);
        config.addDataSourceProperty("db", database);
        config.addDataSourceProperty("user", datasourceConfig.getUsername());
        config.addDataSourceProperty("password", datasourceConfig.getPassword());
    }
}
