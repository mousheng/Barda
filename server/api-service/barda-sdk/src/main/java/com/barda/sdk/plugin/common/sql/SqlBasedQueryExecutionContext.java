package com.barda.sdk.plugin.common.sql;

import static org.apache.commons.collections4.MapUtils.emptyIfNull;

import java.util.Map;

import com.barda.sdk.plugin.sqlcommand.GuiSqlCommand;
import com.barda.sdk.query.QueryExecutionContext;

import lombok.Builder;
import lombok.Getter;

/**
 * SqlBasedQueryExecutionContext是一个查询执行上下文类，用于基于SQL的查询。
 */
@Getter
public class SqlBasedQueryExecutionContext extends QueryExecutionContext {

    /**
     * 查询语句
     */
    private final String query;

    /**
     * 请求参数
     */
    private final Map<String, Object> requestParams;

    /**
     * 是否禁用PreparedStatement
     */
    private final boolean disablePreparedStatement;

    /**
     * GUI命令
     */
    private final GuiSqlCommand guiSqlCommand;

    /**
     * 构造方法，用于创建SqlBasedQueryExecutionContext实例。
     *
     * @param query                     查询语句
     * @param requestParams             请求参数
     * @param disablePreparedStatement 是否禁用PreparedStatement
     * @param guiSqlCommand             GUI命令
     */
    @Builder
    private SqlBasedQueryExecutionContext(String query, Map<String, Object> requestParams, boolean disablePreparedStatement,
            GuiSqlCommand guiSqlCommand) {
        this.query = query;
        this.requestParams = requestParams;
        this.disablePreparedStatement = disablePreparedStatement;
        this.guiSqlCommand = guiSqlCommand;
    }

    /**
     * 获取请求参数。
     *
     * @return 请求参数，如果为null，则返回一个空Map
     */
    public Map<String, Object> getRequestParams() {
        return emptyIfNull(requestParams);
    }

    /**
     * 返回一个用于构建当前对象的builder。
     *
     * @return 用于构建SqlBasedQueryExecutionContext的builder
     */
    public SqlBasedQueryExecutionContextBuilder toBuilder() {
        return SqlBasedQueryExecutionContext.builder()
                .query(query)
                .requestParams(requestParams)
                .disablePreparedStatement(disablePreparedStatement)
                .guiSqlCommand(guiSqlCommand);
    }
}
