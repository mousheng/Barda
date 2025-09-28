package com.barda.plugin.clickhouse.model;

import static com.barda.sdk.exception.PluginCommonError.INVALID_QUERY_SETTINGS;
import static com.barda.sdk.util.JsonUtils.fromJson;
import static com.barda.sdk.util.JsonUtils.toJson;

import java.util.Map;

import org.apache.commons.collections4.MapUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.barda.sdk.exception.PluginException;

import lombok.Getter;

/**
 * ClickHouseQueryConfig 类用于表示 ClickHouse 查询操作的配置。
 */
@Getter
public class ClickHouseQueryConfig {

    /**
     * 要执行的 SQL 查询。
     */
    private final String sql;

    /**
     * 是否禁用 PreparedStatement。
     */
    private final boolean disablePreparedStatement;

    /**
     * 查询操作的超时时间（单位：毫秒）。
     */
    private final int timeout;

    /**
     * 私有构造函数，用于创建 ClickHouseQueryConfig 类的实例。
     *
     * @param sql SQL 查询
     * @param disablePreparedStatement 是否禁用 PreparedStatement
     * @param timeout 超时时间
     */
    @JsonCreator
    private ClickHouseQueryConfig(String sql, boolean disablePreparedStatement, int timeout) {
        this.sql = sql;
        this.disablePreparedStatement = disablePreparedStatement;
        this.timeout = timeout;
    }

    /**
     * 从 Map 中构建 ClickHouseQueryConfig 类的实例。
     *
     * @param queryConfigs 查询配置的 Map
     * @return ClickHouseQueryConfig 类的实例
     * @throws PluginException 如果构建失败
     */
    public static ClickHouseQueryConfig from(Map<String, Object> queryConfigs) {
        if (MapUtils.isEmpty(queryConfigs)) {
            throw new PluginException(INVALID_QUERY_SETTINGS, "CLICKHOUSE_CONFIG_EMPTY");
        }

        ClickHouseQueryConfig result = fromJson(toJson(queryConfigs), ClickHouseQueryConfig.class);
        if (result == null) {
            throw new PluginException(INVALID_QUERY_SETTINGS, "INVALID_CLICKHOUSE");
        }
        return result;
    }

    /**
     * 获取去除前后空格的 SQL 查询。
     *
     * @return 去除前后空格的 SQL 查询
     */
    public String getSql() {
        return sql.trim();
    }
}