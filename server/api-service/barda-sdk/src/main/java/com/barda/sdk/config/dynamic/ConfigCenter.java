package com.barda.sdk.config.dynamic;

/**
 * 该接口表示一个配置中心。
 *
 * 该接口定义了一些方法来获取不同类型的配置实例。
 */
public interface ConfigCenter {

    /**
     * 获取资产配置。
     *
     * @return 资产配置
     */
    ConfigInstance asset();

    /**
     * 获取 MySQL 插件的配置。
     *
     * @return MySQL 插件的配置
     */
    ConfigInstance mysqlPlugin();

    /**
     * 获取 ClickHouse 插件的配置。
     *
     * @return ClickHouse 插件的配置
     */
    ConfigInstance clickHousePlugin();

    /**
     * 获取 MongoDB 插件的配置。
     *
     * @return MongoDB 插件的配置
     */
    ConfigInstance mongoPlugin();

    /**
     * 获取 PostgreSQL 插件的配置。
     *
     * @return PostgreSQL 插件的配置
     */
    ConfigInstance postgresPlugin();

    /**
     * 获取 Oracle 插件的配置。
     *
     * @return Oracle 插件的配置
     */
    ConfigInstance oraclePlugin();

    /**
     * 获取阈值配置。
     *
     * @return 阈值配置
     */
    ConfigInstance threshold();

    /**
     * 获取代理配置。
     *
     * @return 代理配置
     */
    ConfigInstance proxy();

    /**
     * 获取身份验证配置。
     *
     * @return 身份验证配置
     */
    ConfigInstance auth();

    /**
     * 获取数据源配置。
     *
     * @return 数据源配置
     */
    ConfigInstance datasource();

    /**
     * 获取部署配置。
     *
     * @return 部署配置
     */
    ConfigInstance deployment();

    /**
     * 获取应用配置。
     *
     * @return 应用配置
     */
    ConfigInstance application();
}
