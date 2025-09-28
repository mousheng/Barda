package com.barda.sdk.plugin.sqlcommand.command;

import java.util.List;

import com.barda.sdk.plugin.sqlcommand.GuiSqlCommand.GuiSqlCommandRenderResult;

/**
 * 用于更新或删除单个记录的命令渲染结果类，继承自 {@link GuiSqlCommandRenderResult}。
 * 该类包含了执行更新或删除操作所需的 SELECT 查询和 UPDATE/DELETE SQL 语句。
 */
public class UpdateOrDeleteSingleCommandRenderResult extends GuiSqlCommandRenderResult {

    /**
     * 执行 SELECT 操作的 SQL 查询。
     */
    private final String selectQuery;

    /**
     * 执行 SELECT 操作的 SQL 查询所需的绑定参数。
     */
    private final List<Object> selectBindParams;

    /**
     * 构造函数，使用提供的 SELECT 查询、SELECT 查询的绑定参数、UPDATE/DELETE SQL 查询、UPDATE/DELETE 查询的绑定参数来初始化
     * UpdateOrDeleteSingleCommandRenderResult 实例。
     */
    public UpdateOrDeleteSingleCommandRenderResult(String selectQuery, List<Object> selectBindParams, String updateOrDeleteSql,
            List<Object> updateBindParams) {
        super(updateOrDeleteSql, updateBindParams);
        this.selectQuery = selectQuery;
        this.selectBindParams = selectBindParams;
    }

    /**
     * 获取执行 SELECT 操作的 SQL 查询。
     *
     * @return 执行 SELECT 操作的 SQL 查询。
     */
    public String getSelectQuery() {
        return selectQuery;
    }

    /**
     * 获取执行 SELECT 操作的 SQL 查询所需的绑定参数。
     *
     * @return 执行 SELECT 操作的 SQL 查询所需的绑定参数。
     */
    public List<Object> getSelectBindParams() {
        return selectBindParams;
    }
}