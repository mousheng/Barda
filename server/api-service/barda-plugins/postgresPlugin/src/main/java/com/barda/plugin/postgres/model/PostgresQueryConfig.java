package com.barda.plugin.postgres.model;

import static com.barda.sdk.exception.PluginCommonError.INVALID_QUERY_SETTINGS;
import static com.barda.sdk.util.JsonUtils.fromJson;
import static com.barda.sdk.util.JsonUtils.toJson;

import java.util.Map;

import org.apache.commons.collections4.MapUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.barda.sdk.exception.PluginException;

import lombok.Getter;

/**
 * PostgreSQL 查询配置类。
 * 包含 SQL 查询、PreparedStatement 禁用、模式（GUI/非 GUI）等配置。
 */
@Getter
public class PostgresQueryConfig {

    private final String sql;
    private final boolean disablePreparedStatement;

    private final String mode;

    private final String guiStatementType;
    private final Map<String, Object> guiStatementDetail;

    /**
     * 构造器。
     *
     * @param sql                      SQL 查询
     * @param disablePreparedStatement 是否禁用 PreparedStatement
     * @param mode                     模式（GUI/非 GUI）
     * @param guiStatementType         GUI 模式下的命令类型
     * @param guiStatementDetail       GUI 模式下的命令详细信息
     */
    @JsonCreator
    private PostgresQueryConfig(String sql, boolean disablePreparedStatement,
            String mode,
            @JsonProperty("commandType") String guiStatementType,
            @JsonProperty("command") Map<String, Object> guiStatementDetail) {
        this.sql = sql;
        this.disablePreparedStatement = disablePreparedStatement;
        this.mode = mode;
        this.guiStatementType = guiStatementType;
        this.guiStatementDetail = guiStatementDetail;
    }

    /**
     * 从 Map 构建 PostgresQueryConfig 实例。
     *
     * @param queryConfigs 包含查询配置的 Map
     * @return 构建的 PostgresQueryConfig 实例
     * @throws PluginException 如果构建失败
     */
    public static PostgresQueryConfig from(Map<String, Object> queryConfigs) {
        if (MapUtils.isEmpty(queryConfigs)) {
            throw new PluginException(INVALID_QUERY_SETTINGS, "INVALID_PG_QUERY_CONFIG_EMPTY");
        }

        PostgresQueryConfig result = fromJson(toJson(queryConfigs), PostgresQueryConfig.class);
        if (result == null) {
            throw new PluginException(INVALID_QUERY_SETTINGS, "INVALID_PG");
        }
        return result;
    }

    /**
     * 判断是否为 GUI 模式。
     *
     * @return true - GUI 模式；false - 非 GUI 模式
     */
    public boolean isGuiMode() {
        return "GUI".equalsIgnoreCase(mode);
    }

}
