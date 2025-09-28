package com.barda.sdk.plugin.common.sql;

import java.util.Properties;
import java.util.function.Supplier;

/**
 * HikariPerfWrapper用于包装HikariCP数据源，并提供性能相关的信息。
 */
public class HikariPerfWrapper {

    /**
     * Hikari数据源对象
     */
    private final Object hikariDataSource;

    /**
     * 总连接数的供应器
     */
    private final Supplier<Integer> totalConnections;

    /**
     * 空闲连接数的供应器
     */
    private final Supplier<Integer> idleConnections;

    /**
     * 活动连接数的供应器
     */
    private final Supplier<Integer> activeConnections;

    /**
     * 等待连接数的供应器
     */
    private final Supplier<Integer> awaitConnections;

    /**
     * 数据源属性的供应器
     */
    private final Supplier<Properties> datasourceProperties;

    /**
     * 健康检查属性的供应器
     */
    private final Supplier<Properties> healthCheckProperties;

    /**
     * 私有构造方法，用于创建HikariPerfWrapper实例。
     *
     * @param hikariDataSource     Hikari数据源对象
     * @param totalConnections     总连接数的供应器
     * @param idleConnections      空闲连接数的供应器
     * @param activeConnections    活动连接数的供应器
     * @param awaitConnections     等待连接数的供应器
     * @param datasourceProperties 数据源属性的供应器
     * @param healthCheckProperties 健康检查属性的供应器
     */
    private HikariPerfWrapper(Object hikariDataSource, Supplier<Integer> totalConnections,
            Supplier<Integer> idleConnections,
            Supplier<Integer> activeConnections,
            Supplier<Integer> awaitConnections, Supplier<Properties> datasourceProperties, Supplier<Properties> healthCheckProperties) {
        this.hikariDataSource = hikariDataSource;
        this.totalConnections = totalConnections;
        this.idleConnections = idleConnections;
        this.activeConnections = activeConnections;
        this.awaitConnections = awaitConnections;
        this.datasourceProperties = datasourceProperties;
        this.healthCheckProperties = healthCheckProperties;
    }

    /**
     * 包装给定的Hikari数据源及其性能信息。
     *
     * @param hikariDataSource     Hikari数据源对象
     * @param totalConnections     总连接数的供应器
     * @param idleConnections      空闲连接数的供应器
     * @param activeConnections    活动连接数的供应器
     * @param awaitConnections     等待连接数的供应器
     * @param datasourceProperties 数据源属性的供应器
     * @param healthCheckProperties 健康检查属性的供应器
     * @return 包装了性能信息的HikariPerfWrapper实例
     */
    public static HikariPerfWrapper wrap(Object hikariDataSource,
            Supplier<Integer> totalConnections,
            Supplier<Integer> idleConnections,
            Supplier<Integer> activeConnections,
            Supplier<Integer> awaitConnections,
            Supplier<Properties> datasourceProperties,
            Supplier<Properties> healthCheckProperties) {
        return new HikariPerfWrapper(hikariDataSource, totalConnections, idleConnections, activeConnections, awaitConnections, datasourceProperties,
                healthCheckProperties);
    }

    /**
     * 获取被包装的Hikari数据源对象。
     *
     * @return Hikari数据源对象
     */
    public Object getHikariDataSource() {
        return hikariDataSource;
    }

    /**
     * 获取总连接数。
     *
     * @return 总连接数
     */
    public int getTotalConnections() {
        return totalConnections.get();
    }

    /**
     * 获取空闲连接数。
     *
     * @return 空闲连接数
     */
    public int getIdleConnections() {
        return idleConnections.get();
    }

    /**
     * 获取活动连接数。
     *
     * @return 活动连接数
     */
    public int getActiveConnections() {
        return activeConnections.get();
    }

    /**
     * 获取等待连接数。
     *
     * @return 等待连接数
     */
    public int getWaitingConnections() {
        return awaitConnections.get();
    }

    /**
     * 获取数据源属性。
     *
     * @return 数据源属性
     */
    public Properties getDatasourceProperties() {
        return datasourceProperties.get();
    }

    /**
     * 获取健康检查属性。
     *
     * @return 健康检查属性
     */
    public Properties getHealthCheckProperties() {
        return healthCheckProperties.get();
    }
}
