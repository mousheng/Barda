package com.barda.plugin.sql;

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
 * 用于存储和解析SQL查询配置的类。
 * 该类包含了SQL查询的相关信息，如SQL语句、是否禁用PreparedStatement、查询模式等。
 */
@Getter
public class SqlQueryConfig {

    private final String sql;
    private final boolean disablePreparedStatement;
    private final String mode;

    private final String guiStatementType;
    private final Map<String, Object> guiStatementDetail;

    /**
     * 构造函数。
     *
     * @param sql SQL语句
     * @param disablePreparedStatement 是否禁用PreparedStatement
     * @param mode 查询模式
     * @param guiStatementType GUI命令类型
     * @param guiStatementDetail GUI命令的详细信息
     */
    @JsonCreator
    private SqlQueryConfig(String sql, boolean disablePreparedStatement,
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
     * 从Map中创建SqlQueryConfig的实例。
     *
     * @param queryConfigs 包含查询配置的Map
     * @return SqlQueryConfig的实例
     */
    public static SqlQueryConfig from(Map<String, Object> queryConfigs) {
        if (MapUtils.isEmpty(queryConfigs)) {
            throw new PluginException(INVALID_QUERY_SETTINGS, "EMPTY_SQL_QUERY_CONFIG");
        }

        SqlQueryConfig result = fromJson(toJson(queryConfigs), SqlQueryConfig.class);
        if (result == null) {
            throw new PluginException(INVALID_QUERY_SETTINGS, "INVALID_SQL_QUERY_CONFIG");
        }
        return result;
    }

    /**
     * 判断是否为GUI模式。
     *
     * @return true表示为GUI模式，false表示为非GUI模式
     */
    public boolean isGuiMode() {
        return "GUI".equalsIgnoreCase(mode);
    }

    /**
     * 获取SQL语句，并去掉前后的空格。
     *
     * @return 去掉前后空格的SQL语句
     */
    public String getSql() {
        return sql.trim();
    }
}
