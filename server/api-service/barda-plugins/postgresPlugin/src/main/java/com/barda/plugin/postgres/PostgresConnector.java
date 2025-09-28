package com.barda.plugin.postgres;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import org.apache.commons.lang3.StringUtils;
import org.pf4j.Extension;

import com.barda.plugin.postgres.model.PostgresDatasourceConfig;
import com.barda.plugin.sql.SqlBasedConnector;
import com.zaxxer.hikari.HikariConfig;

/**
 * 一个用于与PostgreSQL数据库进行交互的SQL-based连接器。
 *
 * 该类继承自 {@link SqlBasedConnector} 并使用了Lombok的 {@link Extension} 注解来标记为一个扩展点。
 *
 */
@Extension
public class PostgresConnector extends SqlBasedConnector<PostgresDatasourceConfig> {

    /**
     * 构造函数。
     *
     * 该构造函数调用父类构造函数并指定初始并发度为100。
     */
    public PostgresConnector() {
        super(100);
    }

    /**
     * 获取JDBC驱动的类名。
     *
     * @return JDBC驱动的类名
     */
    @Override
    protected String getJdbcDriver() {
        return "org.postgresql.Driver";
    }

    /**
     * 设置数据源配置。
     *
     * @param datasourceConfig 数据源配置
     * @param config HikariCP配置
     */
    @Override
    protected void setUpConfigs(PostgresDatasourceConfig datasourceConfig, HikariConfig config) {

        // 设置认证属性
        String username = datasourceConfig.getUsername();
        if (StringUtils.isNotEmpty(username)) {
            config.setUsername(username);
        }
        String password = datasourceConfig.getPassword();
        if (StringUtils.isNotEmpty(password)) {
            config.setPassword(password);
        }

        String host = datasourceConfig.getHost();
        long port = datasourceConfig.getPort();
        String database = datasourceConfig.getDatabase();
        String url = "jdbc:postgresql://" + host + ":" + port + "/" + (isNotBlank(database) ? database : "");
        config.setJdbcUrl(url);

        if (datasourceConfig.isUsingSsl()) {
            config.addDataSourceProperty("ssl", "true");
            config.addDataSourceProperty("sslmode", "require");
        } else {
            config.addDataSourceProperty("ssl", "false");
            config.addDataSourceProperty("sslmode", "disable");
        }

        if (datasourceConfig.isReadonly()) {
            config.setReadOnly(true);
            config.addDataSourceProperty("readOnlyMode", "always");
        } else {
            config.setReadOnly(false);
        }
    }
}
